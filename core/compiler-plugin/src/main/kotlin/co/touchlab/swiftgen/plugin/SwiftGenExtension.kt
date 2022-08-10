package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.plugin.internal.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.SwiftGenVisitor
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SwiftGenExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()
            val namespaceProvider = NamespaceProvider(fileBuilderFactory, moduleFragment)

            SwiftGenVisitor(fileBuilderFactory, namespaceProvider).visitElement(moduleFragment, Unit)

            fileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }
}
