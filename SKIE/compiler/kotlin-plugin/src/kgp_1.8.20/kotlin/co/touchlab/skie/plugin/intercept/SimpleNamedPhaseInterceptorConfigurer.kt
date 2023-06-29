package co.touchlab.skie.plugin.intercept

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.SimpleNamedCompilerPhase
import org.jetbrains.kotlin.backend.konan.ConfigChecks
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import kotlin.reflect.jvm.jvmName

internal class SimpleNamedPhaseInterceptorConfigurer<Context, Input, Output>:
    PhaseInterceptorConfigurer<SimpleNamedCompilerPhase<Context, Input, Output>, Context, Input, Output>
    where Context: LoggingContext, Context: ConfigChecks
{
    override fun canConfigurePhase(phase: Any): Boolean = phase is SimpleNamedCompilerPhase<*, *, *>

    override fun configure(
        configuration: CompilerConfiguration,
        phase: SimpleNamedCompilerPhase<Context, Input, Output>,
        interceptors: List<PhaseInterceptor<Context, Input, Output>>,
    ) {
        val namedPhase = phase.reflector
        val chain = ErasedPhaseInterceptorChain(interceptors)
        synchronized(phase) {
            val currentPhaseBody = namedPhase.phaseBody
            val (originalPhaseBody, interceptorKey) = if (currentPhaseBody.isIntercepted()) {
                val interceptedPhaseBody = InterceptedPhaseBodyReflector(currentPhaseBody)
                interceptedPhaseBody.originalPhaseBody to interceptedPhaseBody.interceptorKey
            } else {
                currentPhaseBody to CompilerConfigurationKey.create("phaseInterceptor for phase $phase")
            }

            configuration.put(interceptorKey, chain)

            val interceptedPhaseBody = InterceptedPhaseBody(
                originalPhaseBody,
                interceptorKey,
            )
            namedPhase.phaseBody = interceptedPhaseBody
        }
    }

    private val SimpleNamedCompilerPhase<Context, Input, Output>.reflector: SimpleNamedCompilerPhaseReflector<Context, Input, Output>
        get() = SimpleNamedCompilerPhaseReflector(this)
}

private class InterceptedPhaseBody<Context, Input, Output>(
    val originalPhaseBody: OriginalPhaseBody<Context, Input, Output>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>>,
): (Context, Input) -> Output where Context: LoggingContext, Context: ConfigChecks {
    override fun invoke(context: Context, input: Input): Output {
        val interceptor = context.config.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input, originalPhaseBody)
        } else {
            originalPhaseBody.invoke(context, input)
        }
    }
}

private class SimpleNamedCompilerPhaseReflector<Context, Input, Output>(
    override val instance: SimpleNamedCompilerPhase<Context, Input, Output>,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    var phaseBody: (Context, Input) -> Output by declaredField("\$phaseBody")
}

private class InterceptedPhaseBodyReflector<Context, Input, Output>(
    override val instance: (Context, Input) -> Output,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    val originalPhaseBody: (Context, Input) -> Output by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>> by declaredField()
}

private fun <Context, Input, Output> ((Context, Input) -> Output).isIntercepted(): Boolean where Context: LoggingContext, Context: ConfigChecks {
    return javaClass.name == InterceptedPhaseBody::class.jvmName
}
