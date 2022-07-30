package co.touchlab.swiftpack.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

interface SwiftPackGenerationExtension {

    fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext)

}