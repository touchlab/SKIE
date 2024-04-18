package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase

object AwaitAllBackgroundJobsPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        awaitAllBackgroundJobs()
    }
}
