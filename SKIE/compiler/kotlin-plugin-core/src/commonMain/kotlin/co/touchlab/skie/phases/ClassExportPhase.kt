package co.touchlab.skie.phases

interface ClassExportPhase<in C : ClassExportPhase.Context> : ForegroundPhase<C> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
