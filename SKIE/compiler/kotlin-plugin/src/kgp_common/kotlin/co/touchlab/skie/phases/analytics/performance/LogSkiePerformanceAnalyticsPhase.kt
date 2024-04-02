package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.phases.LinkPhase

// Cannot run in the background otherwise there is a risk of a deadlock
object LogSkiePerformanceAnalyticsPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectSynchronously(skiePerformanceAnalyticsProducer)
    }
}
