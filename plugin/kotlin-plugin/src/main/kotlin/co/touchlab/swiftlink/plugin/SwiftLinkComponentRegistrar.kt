package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.plugin.internal.SwiftGenIrGenerationExtension
import co.touchlab.swiftlink.plugin.intercept.PhaseInterceptor
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftLinkComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configuration.get(ConfigurationKeys.linkPhaseSwiftPackOutputDir)?.let {
            SwiftPackModuleBuilder.Config.outputDir = it
        }

        IrGenerationExtension.registerExtension(project, SwiftGenIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
