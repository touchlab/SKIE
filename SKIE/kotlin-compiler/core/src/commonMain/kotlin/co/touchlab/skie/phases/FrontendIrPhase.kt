package co.touchlab.skie.phases

interface FrontendIrPhase : ForegroundPhase<FrontendIrPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
