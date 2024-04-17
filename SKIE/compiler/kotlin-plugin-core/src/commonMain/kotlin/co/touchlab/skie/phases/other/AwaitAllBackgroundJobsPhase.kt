package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.CompilerIndependentLinkPhase
import co.touchlab.skie.phases.LinkPhase

object AwaitAllBackgroundJobsPhase : CompilerIndependentLinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        awaitAllBackgroundJobs()
    }
}
