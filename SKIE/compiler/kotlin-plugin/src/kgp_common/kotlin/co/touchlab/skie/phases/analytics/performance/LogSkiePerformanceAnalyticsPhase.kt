package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.phases.FinalizePhase

object LogSkiePerformanceAnalyticsPhase : FinalizePhase {

    context(FinalizePhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectAsync(skiePerformanceAnalyticsProducer)
    }
}
