@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.ForegroundCompilerPhase
import co.touchlab.skie.phases.KotlinIrPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable

class KotlinIrPhaseContext(
    mainSkieContext: MainSkieContext,
    override val moduleFragment: IrModuleFragment,
    override val pluginContext: IrPluginContext,
) : KotlinIrPhase.Context, ForegroundCompilerPhase.Context by mainSkieContext {

    override val context: KotlinIrPhaseContext = this

    private val linker = ((pluginContext as? IrPluginContextImpl)?.linker as KonanIrLinker)

    override val allModules: Map<String, IrModuleFragment> = linker.modules

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder

    override val skieSymbolTable: SkieSymbolTable = SkieSymbolTable(pluginContext.symbolTable as SymbolTable)
}
