package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.phases.SirPhase

object LogSkiePerformanceAnalyticsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        analyticsCollector.collectAsync(skiePerformanceAnalyticsProducer)
    }
}
