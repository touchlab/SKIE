package co.touchlab.swiftgen.sealed.irinspector.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class IrInspectorPluginComponentRegistrar: ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val extension = createExtension(configuration)

        registerExtension(project, extension)
    }

    private fun createExtension(configuration: CompilerConfiguration): IrInspectorPluginExtension {
        val outputPath = IrInspectorPluginCompilerConfiguration.OutputPath.get(configuration)

        return IrInspectorPluginExtension(outputPath)
    }

    private fun registerExtension(project: MockProject, extension: IrInspectorPluginExtension) {
        val extensionPoint = project.extensionArea.getExtensionPoint(IrGenerationExtension.extensionPointName)

        extensionPoint.registerExtension(extension, LoadingOrder.LAST, project)
    }
}
