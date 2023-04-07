package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl

import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class GenerateIrPhase(
    private val declarationBuilder: DeclarationBuilderImpl,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        declarationBuilder.generateIr(moduleFragment, pluginContext)
    }
}
