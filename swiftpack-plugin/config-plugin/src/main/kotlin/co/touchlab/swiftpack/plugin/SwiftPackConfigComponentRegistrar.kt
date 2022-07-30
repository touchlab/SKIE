package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.util.ServiceLoader

@AutoService(ComponentRegistrar::class)
class SwiftPackConfigComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val outputDir = configuration.get(SwiftPackConfigurationKeys.outputDir)
        SwiftPackModuleBuilder.Config.outputDir = outputDir

        IrGenerationExtension.registerExtension(project, SwiftPackGenerationExtensionRunner())
    }
}

