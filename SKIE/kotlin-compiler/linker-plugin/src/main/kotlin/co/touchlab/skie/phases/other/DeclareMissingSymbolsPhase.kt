package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SymbolTablePhase
import co.touchlab.skie.phases.declarationBuilder

object DeclareMissingSymbolsPhase : SymbolTablePhase {

    context(context: SymbolTablePhase.Context)
    override suspend fun execute() {
        context.declarationBuilder.declareSymbols()
    }
}
