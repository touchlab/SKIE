package co.touchlab.skie.compilerinject.interceptor

import co.touchlab.skie.compilerinject.interceptor.ErasedPhaseInterceptorConfigurer
import co.touchlab.skie.compilerinject.interceptor.NamedPhaseInterceptorConfigurer
import org.jetbrains.kotlin.config.CompilerConfiguration

actual fun phaseInterceptorConfigurers(configuration: CompilerConfiguration): List<ErasedPhaseInterceptorConfigurer> {
    return listOf(
        NamedPhaseInterceptorConfigurer(),
    )
}
