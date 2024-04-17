package co.touchlab.skie.phases

interface CompilerIndependentClassExportPhase : ClassExportPhase<CompilerIndependentClassExportPhase.Context> {

    interface Context : ClassExportPhase.Context {

        override val context: Context
    }
}
