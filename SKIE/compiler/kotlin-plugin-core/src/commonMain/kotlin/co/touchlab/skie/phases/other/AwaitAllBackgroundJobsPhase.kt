package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkCorePhase
import co.touchlab.skie.phases.LinkPhase

object AwaitAllBackgroundJobsPhase : LinkCorePhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        awaitAllBackgroundJobs()
    }
}
