package co.touchlab.skie.phases

interface FinalizePhase : SkiePhase<FinalizePhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context
    }
}
