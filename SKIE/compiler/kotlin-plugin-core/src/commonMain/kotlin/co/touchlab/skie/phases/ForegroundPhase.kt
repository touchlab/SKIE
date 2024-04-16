package co.touchlab.skie.phases

interface ForegroundPhase<in C : ForegroundPhase.Context> : ScheduledPhase<C> {

    interface Context : ScheduledPhase.Context {

        override val context: Context
    }
}
