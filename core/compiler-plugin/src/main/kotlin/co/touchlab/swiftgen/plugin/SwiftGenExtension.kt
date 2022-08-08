package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.plugin.internal.SwiftGenVisitor
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SwiftGenExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen") {
            SwiftGenVisitor(this, moduleFragment).visitElement(moduleFragment, Unit)
        }
    }
}
