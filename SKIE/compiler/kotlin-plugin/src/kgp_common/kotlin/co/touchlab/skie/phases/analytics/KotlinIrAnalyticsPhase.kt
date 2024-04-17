package co.touchlab.skie.phases.analytics

import co.touchlab.skie.analytics.modules.ModulesAnalytics
import co.touchlab.skie.phases.KotlinIrPhase

object KotlinIrAnalyticsPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectSynchronously(
            ModulesAnalytics.Producer(context),
        )
    }
}
