package co.touchlab.skie.kir.irbuilder.impl

import co.touchlab.skie.phases.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class GenerateIrPhase(
    private val declarationBuilder: DeclarationBuilderImpl,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        declarationBuilder.generateIr(moduleFragment, pluginContext, pluginContext.symbolTable as SymbolTable)
    }
}
