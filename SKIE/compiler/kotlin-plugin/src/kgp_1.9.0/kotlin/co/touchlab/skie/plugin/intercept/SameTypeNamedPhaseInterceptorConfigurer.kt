package co.touchlab.skie.plugin.intercept

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfigurationService
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.SameTypeNamedCompilerPhase
import org.jetbrains.kotlin.backend.konan.ConfigChecks
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import kotlin.reflect.jvm.jvmName

internal class SameTypeNamedPhaseInterceptorConfigurer<Context, Data>:
    SameTypePhaseInterceptorConfigurer<SameTypeNamedCompilerPhase<Context, Data>, Context, Data> where Context: LoggingContext, Context: ConfigChecks {
    override fun canConfigurePhase(phase: Any): Boolean = phase is SameTypeNamedCompilerPhase<*, *>

    override fun configure(
        configuration: CompilerConfiguration,
        phase: SameTypeNamedCompilerPhase<Context, Data>,
        interceptors: List<PhaseInterceptor<Context, Data, Data>>,
    ) {
        val namedPhase = phase.reflector
        val chain = ErasedPhaseInterceptorChain(interceptors)

        synchronized(phase) {
            val currentPhase = namedPhase.lower
            val (originalPhase, interceptorKey) = if (currentPhase.isIntercepted()) {
                val interceptedPhase = InterceptedSameTypeCompilerPhaseReflector(currentPhase)
                interceptedPhase.originalPhase to interceptedPhase.interceptorKey
            } else {
                currentPhase to CompilerConfigurationKey.create("phaseInterceptor for phase $phase")
            }

            configuration.put(interceptorKey, chain)

            val interceptorPhase = InterceptedSameTypeCompilerPhase(originalPhase, interceptorKey)
            namedPhase.lower = interceptorPhase
        }
    }

    private val SameTypeNamedCompilerPhase<Context, Data>.reflector: SameTypeNamedCompilerPhaseReflector<Context, Data>
        get() = SameTypeNamedCompilerPhaseReflector(this)
}


private class SameTypeNamedCompilerPhaseReflector<Context, Data>(
    override val instance: SameTypeNamedCompilerPhase<Context, Data>,
): Reflector(instance::class) where Context: LoggingContext {
    var lower: SameTypeCompilerPhase<Context, Data> by declaredField()
}

private class InterceptedSameTypeCompilerPhaseReflector<Context, Data>(
    override val instance: SameTypeCompilerPhase<Context, Data>,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    val originalPhase: SameTypeCompilerPhase<Context, Data> by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>> by declaredField()
}

private class InterceptedSameTypeCompilerPhase<Context, Data>(
    val originalPhase: SameTypeCompilerPhase<Context, Data>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>>,
): SameTypeCompilerPhase<Context, Data> where Context: LoggingContext, Context: ConfigChecks {
    override fun invoke(phaseConfig: PhaseConfigurationService, phaserState: PhaserState<Data>, context: Context, input: Data): Data {
        val interceptor = context.config.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input) { innerContext, innerInput ->
                originalPhase.invoke(phaseConfig, phaserState, innerContext, innerInput)
            }
        } else {
            originalPhase.invoke(phaseConfig, phaserState, context, input)
        }
    }
}

private fun <Context, Data> SameTypeCompilerPhase<Context, Data>.isIntercepted(): Boolean where Context: LoggingContext, Context: ConfigChecks {
    return javaClass.name == InterceptedSameTypeCompilerPhase::class.jvmName
}
