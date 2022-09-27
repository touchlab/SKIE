package co.touchlab.swiftgen.plugin.internal

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SwiftGenIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val irGenerator = SwiftGenCompilerConfigurationKey.IrGenerator.getOrNull(configuration)

        irGenerator?.generateIr(moduleFragment, pluginContext)
    }
}
