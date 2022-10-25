package co.touchlab.skie.plugin.generator.internal

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SwiftGenIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val declarationBuilder = SwiftGenCompilerConfigurationKey.DeclarationBuilder.getOrNull(configuration)

        declarationBuilder?.generateIr(moduleFragment, pluginContext)
    }
}
