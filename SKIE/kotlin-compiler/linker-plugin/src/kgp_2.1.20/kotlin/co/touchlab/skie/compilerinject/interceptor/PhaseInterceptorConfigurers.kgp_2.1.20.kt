package co.touchlab.skie.compilerinject.interceptor

import org.jetbrains.kotlin.config.CompilerConfiguration

actual fun phaseInterceptorConfigurers(configuration: CompilerConfiguration): List<ErasedPhaseInterceptorConfigurer> = listOf(
    SameTypeNamedPhaseInterceptorConfigurer(),
    SimpleNamedPhaseInterceptorConfigurer(),
)
