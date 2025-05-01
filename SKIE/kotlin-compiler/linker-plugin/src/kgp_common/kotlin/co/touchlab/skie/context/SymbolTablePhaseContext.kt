package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.SymbolTablePhase
import org.jetbrains.kotlin.ir.util.SymbolTable

class SymbolTablePhaseContext(mainSkieContext: MainSkieContext, symbolTable: SymbolTable) :
    SymbolTablePhase.Context,
    ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: SymbolTablePhaseContext = this

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder

    val skieSymbolTable: SkieSymbolTable = SkieSymbolTable(symbolTable)
}
