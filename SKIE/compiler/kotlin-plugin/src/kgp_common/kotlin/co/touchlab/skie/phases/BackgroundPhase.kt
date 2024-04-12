package co.touchlab.skie.phases

// Can run in parallel with the Kotlin compiler and other SKIE phases, cannot use anything from the shared context that is not thread-safe
interface BackgroundPhase<C : BackgroundPhase.Context> : ScheduledPhase<C> {

    interface Context : ScheduledPhase.Context {

        override val context: Context
    }
}
