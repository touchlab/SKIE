package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.ForegroundPhase
import co.touchlab.skie.phases.SymbolTablePhase
import org.jetbrains.kotlin.ir.util.SymbolTable

class SymbolTablePhaseContext(
    mainSkieContext: MainSkieContext,
    symbolTable: SymbolTable,
) : SymbolTablePhase.Context, ForegroundPhase.Context by mainSkieContext {

    override val context: SymbolTablePhaseContext = this

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder

    override val skieSymbolTable: SkieSymbolTable = SkieSymbolTable(symbolTable)
}
