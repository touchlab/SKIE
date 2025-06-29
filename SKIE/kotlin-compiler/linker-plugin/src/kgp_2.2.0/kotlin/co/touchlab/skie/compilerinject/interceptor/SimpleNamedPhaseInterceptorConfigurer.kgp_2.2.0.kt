package co.touchlab.skie.compilerinject.interceptor

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.backend.konan.ConfigChecks
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.LoggingContext
import org.jetbrains.kotlin.config.phaser.NamedCompilerPhase
import kotlin.reflect.jvm.jvmName

class SimpleNamedPhaseInterceptorConfigurer<Context, Input, Output> :
    PhaseInterceptorConfigurer<NamedCompilerPhase<Context, Input, Output>, Context, Input, Output>
    where Context : LoggingContext, Context : ConfigChecks {

    override fun canConfigurePhase(phase: Any): Boolean = phase is NamedCompilerPhase<*, *, *>

    override fun configure(
        configuration: CompilerConfiguration,
        phase: NamedCompilerPhase<Context, Input, Output>,
        interceptors: List<PhaseInterceptor<Context, Input, Output>>,
    ) {
        val namedPhase = phase.reflector
        val chain = ErasedPhaseInterceptorChain(interceptors)
        synchronized(phase) {
            val currentPhaseBody = namedPhase.op
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
            namedPhase.op = interceptedPhaseBody
        }
    }

    private val NamedCompilerPhase<Context, Input, Output>.reflector: NamedCompilerPhaseReflector<Context, Input, Output>
        get() = NamedCompilerPhaseReflector(this)
}

private class InterceptedPhaseBody<Context, Input, Output>(
    val originalPhaseBody: OriginalPhaseBody<Context, Input, Output>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>>,
) : (Context, Input) -> Output where Context : LoggingContext, Context : ConfigChecks {

    override fun invoke(context: Context, input: Input): Output {
        val interceptor = context.config.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input, originalPhaseBody)
        } else {
            originalPhaseBody.invoke(context, input)
        }
    }
}

private class NamedCompilerPhaseReflector<Context, Input, Output>(
    override val instance: NamedCompilerPhase<Context, Input, Output>,
) : Reflector(instance::class) where Context : LoggingContext, Context : ConfigChecks {

    var op: (Context, Input) -> Output by declaredField("\$op")
}

private class InterceptedPhaseBodyReflector<Context, Input, Output>(
    override val instance: (Context, Input) -> Output,
) : Reflector(instance::class) where Context : LoggingContext, Context : ConfigChecks {

    val originalPhaseBody: (Context, Input) -> Output by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>> by declaredField()
}

private fun <Context, Input, Output> ((Context, Input) -> Output).isIntercepted(): Boolean where Context : LoggingContext, Context : ConfigChecks {
    return javaClass.name == InterceptedPhaseBody::class.jvmName
}
