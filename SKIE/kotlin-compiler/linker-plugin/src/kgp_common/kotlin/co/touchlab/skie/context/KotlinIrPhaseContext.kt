@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.KotlinIrPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable

class KotlinIrPhaseContext(
    mainSkieContext: MainSkieContext,
    val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) : KotlinIrPhase.Context, ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: KotlinIrPhaseContext = this

    private val linker = ((pluginContext as? IrPluginContextImpl)?.linker as KonanIrLinker)

    val allModules: Map<String, IrModuleFragment> = linker.modules

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder

    val skieSymbolTable: SkieSymbolTable = SkieSymbolTable(pluginContext.symbolTable as SymbolTable)
}
