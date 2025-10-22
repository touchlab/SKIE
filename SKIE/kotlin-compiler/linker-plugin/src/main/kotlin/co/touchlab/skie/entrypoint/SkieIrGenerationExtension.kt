package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SkieIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        EntrypointUtils.runKotlinIrPhases(
            mainSkieContext = configuration.mainSkieContext,
            moduleFragment = moduleFragment,
            pluginContext = pluginContext,
        )
    }
}
