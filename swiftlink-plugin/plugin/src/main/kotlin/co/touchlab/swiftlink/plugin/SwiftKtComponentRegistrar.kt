package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.intercept.PhaseInterceptor
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class SwiftKtComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
