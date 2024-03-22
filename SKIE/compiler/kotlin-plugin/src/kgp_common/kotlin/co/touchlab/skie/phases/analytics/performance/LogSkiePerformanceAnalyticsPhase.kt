package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.phases.LinkPhase

object LogSkiePerformanceAnalyticsPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectAsync(skiePerformanceAnalyticsProducer)
    }
}
