package co.touchlab.skie.phases

interface ForegroundPhase<C : ForegroundPhase.Context> : ScheduledPhase<C> {

    interface Context : ScheduledPhase.Context {

        override val context: Context
    }
}
