package co.touchlab.skie.phases

interface KotlinIrPhase : ForegroundPhase<KotlinIrPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
