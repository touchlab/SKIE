package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.CompilerIndependentLinkPhase

object AwaitAllBackgroundJobsPhase : CompilerIndependentLinkPhase {

    context(CompilerIndependentLinkPhase.Context)
    override suspend fun execute() {
        awaitAllBackgroundJobs()
    }
}
