package co.touchlab.skie.phases

interface DescriptorModificationPhase : ForegroundPhase<DescriptorModificationPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context
    }
}
