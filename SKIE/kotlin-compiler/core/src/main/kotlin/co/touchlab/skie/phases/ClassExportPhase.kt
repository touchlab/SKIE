package co.touchlab.skie.phases

interface ClassExportPhase : ForegroundPhase<ClassExportPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
