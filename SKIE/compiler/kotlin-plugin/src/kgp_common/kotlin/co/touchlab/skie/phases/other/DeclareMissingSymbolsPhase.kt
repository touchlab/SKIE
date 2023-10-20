package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SymbolTablePhase

object DeclareMissingSymbolsPhase : SymbolTablePhase {

    context(SymbolTablePhase.Context)
    override fun execute() {
        declarationBuilder.declareSymbols()
    }
}
