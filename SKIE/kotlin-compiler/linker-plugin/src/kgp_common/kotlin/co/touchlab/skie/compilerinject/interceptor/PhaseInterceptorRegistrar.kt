package co.touchlab.skie.compilerinject.interceptor

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.util.ServiceLoaderLite
import java.net.URLClassLoader
import java.util.ServiceLoader

typealias ErasedPhaseInterceptorConfigurer = PhaseInterceptorConfigurer<*, Nothing, Nothing, Nothing>

interface PhaseInterceptorConfigurer<Phase, Context, Input, Output> {

    fun canConfigurePhase(phase: Any): Boolean

    fun configure(configuration: CompilerConfiguration, phase: Phase, interceptors: List<PhaseInterceptor<Context, Input, Output>>)
}

interface SameTypePhaseInterceptorConfigurer<Phase, Context, Data> : PhaseInterceptorConfigurer<Phase, Context, Data, Data>

expect fun phaseInterceptorConfigurers(configuration: CompilerConfiguration): List<ErasedPhaseInterceptorConfigurer>

interface PhaseInterceptor<Context, Input, Output> {

    // We accept any type for the phase, as we need to support different Kotlin Versions.
    // This is a function instead of a read-only property to make sure we're never storing the phase.
    fun getInterceptedPhase(): Any

    fun intercept(context: Context, input: Input, next: (Context, Input) -> Output): Output
}

interface SameTypePhaseInterceptor<Context, Data> : PhaseInterceptor<Context, Data, Data>

object PhaseInterceptorRegistrar {

    fun setupPhaseInterceptors(configuration: CompilerConfiguration) {
        val configurers = phaseInterceptorConfigurers(configuration)

        val phaseInterceptors =
            (this::class.java.classLoader as? URLClassLoader)?.let { ServiceLoaderLite.loadImplementations(it) }
                ?: ServiceLoader.load(PhaseInterceptor::class.java, this::class.java.classLoader)

        phaseInterceptors
            .groupBy { it.getInterceptedPhase() }
            .forEach { (phase, interceptors) ->
                val acceptingConfigurers = configurers.filter { it.canConfigurePhase(phase) }
                val configurer = acceptingConfigurers.singleOrNull()
                    ?: error("Multiple configurers for phase $phase: $acceptingConfigurers (interceptors: $interceptors)")
                @Suppress("UNCHECKED_CAST")
                (configurer as PhaseInterceptorConfigurer<Any, Any, Any, Any>).configure(
                    configuration,
                    phase,
                    interceptors as List<PhaseInterceptor<Any, Any, Any>>,
                )
            }
    }
}
