package co.touchlab.skie.phases

interface CompilerIndependentLinkPhase : LinkPhase<CompilerIndependentLinkPhase.Context> {

    interface Context : LinkPhase.Context {

        override val context: Context
    }
}
