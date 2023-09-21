package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.SymbolTablePhase
import org.jetbrains.kotlin.ir.util.SymbolTable

class SymbolTablePhaseContext(
    mainSkieContext: MainSkieContext,
    override val symbolTable: SymbolTable,
) : SymbolTablePhase.Context, SkiePhase.Context by mainSkieContext {

    override val context: SymbolTablePhaseContext = this

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
