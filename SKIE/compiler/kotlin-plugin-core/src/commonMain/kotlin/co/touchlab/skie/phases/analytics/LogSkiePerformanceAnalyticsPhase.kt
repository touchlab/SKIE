package co.touchlab.skie.phases.analytics

import co.touchlab.skie.phases.CompilerIndependentLinkPhase
import co.touchlab.skie.phases.LinkPhase

// Cannot run in the background otherwise there is a risk of a deadlock
object LogSkiePerformanceAnalyticsPhase : CompilerIndependentLinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectSynchronously(skiePerformanceAnalyticsProducer)
    }
}
