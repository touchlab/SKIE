package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class SwiftGenExtension(private val pluginConfiguration: SwiftGenConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()
            val namespaceProvider = NamespaceProvider(fileBuilderFactory, moduleFragment)
            val swiftGenScheduler = SwiftGenScheduler(fileBuilderFactory, namespaceProvider, pluginConfiguration)

            swiftGenScheduler.process(moduleFragment)

            fileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }
}
