package co.touchlab.skie.phases.analytics

import co.touchlab.skie.phases.LinkPhase

// Cannot run in the background otherwise there is a risk of a deadlock
object LogSkiePerformanceAnalyticsPhase : LinkPhase {

    context(context: LinkPhase.Context)
    override suspend fun execute() {
        context.analyticsCollector.collectSynchronously(context.skiePerformanceAnalyticsProducer)
    }
}
