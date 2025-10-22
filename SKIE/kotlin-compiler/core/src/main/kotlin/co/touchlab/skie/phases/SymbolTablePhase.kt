package co.touchlab.skie.phases

interface SymbolTablePhase : ForegroundPhase<SymbolTablePhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
