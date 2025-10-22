package co.touchlab.skie.compilerinject.interceptor

import org.jetbrains.kotlin.config.CompilerConfiguration

fun phaseInterceptorConfigurers(configuration: CompilerConfiguration): List<ErasedPhaseInterceptorConfigurer> {
    return listOf(
        SameTypeNamedPhaseInterceptorConfigurer(),
        SimpleNamedPhaseInterceptorConfigurer(),
    )
}
