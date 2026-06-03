package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase

object AwaitAllBackgroundJobsPhase : LinkPhase {

    context(context: LinkPhase.Context)
    override suspend fun execute() {
        context.awaitAllBackgroundJobs()
    }
}
