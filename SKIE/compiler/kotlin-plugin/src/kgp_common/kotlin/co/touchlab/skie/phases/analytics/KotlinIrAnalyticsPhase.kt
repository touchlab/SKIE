package co.touchlab.skie.phases.analytics

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.analytics.modules.ModulesAnalytics

object KotlinIrAnalyticsPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectSynchronously(
            ModulesAnalytics.Producer(context),
        )
    }
}
