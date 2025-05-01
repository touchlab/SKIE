package co.touchlab.skie.compilerinject.interceptor

import co.touchlab.skie.compilerinject.reflection.Reflector
import kotlin.reflect.jvm.jvmName
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

class NamedPhaseInterceptorConfigurer<Context : CommonBackendContext, Data> :
    PhaseInterceptorConfigurer<NamedCompilerPhase<Context, Data>, Context, Data, Data> {

    override fun canConfigurePhase(phase: Any): Boolean = phase is NamedCompilerPhase<*, *>

    override fun configure(
        configuration: CompilerConfiguration,
        phase: NamedCompilerPhase<Context, Data>,
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

    private val <Context : CommonBackendContext, Data> NamedCompilerPhase<Context, Data>.reflector: NamedCompilerPhaseReflector<Context, Data>
        get() = NamedCompilerPhaseReflector(this)
}

private class NamedCompilerPhaseReflector<Context : CommonBackendContext, Data>(override val instance: NamedCompilerPhase<Context, Data>) :
    Reflector(instance::class) {

    var lower: SameTypeCompilerPhase<Context, Data> by declaredField()
}

private class InterceptedSameTypeCompilerPhaseReflector<Context : CommonBackendContext, Data>(
    override val instance: SameTypeCompilerPhase<Context, Data>,
) : Reflector(instance::class) {

    val originalPhase: SameTypeCompilerPhase<Context, Data> by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>> by declaredField()
}

private fun <Context : CommonBackendContext, Data> SameTypeCompilerPhase<Context, Data>.isIntercepted(): Boolean =
    javaClass.name == InterceptedSameTypeCompilerPhase::class.jvmName
