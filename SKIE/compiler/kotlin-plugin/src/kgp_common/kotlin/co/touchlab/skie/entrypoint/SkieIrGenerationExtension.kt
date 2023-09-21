package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.context.KotlinIrPhaseContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SkieIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val context = KotlinIrPhaseContext(
            mainSkieContext = configuration.mainSkieContext,
            moduleFragment = moduleFragment,
            pluginContext = pluginContext,
        )

        SkiePhaseScheduler.runKotlinIrPhases(context)
    }
}
