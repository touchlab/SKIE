package co.touchlab.skie.phases.analytics

import co.touchlab.skie.analytics.modules.ModulesAnalytics
import co.touchlab.skie.phases.KotlinIrPhase

object KotlinIrAnalyticsPhase : KotlinIrPhase {

    context(context: KotlinIrPhase.Context)
    override suspend fun execute() {
        context.analyticsCollector.collectSynchronously(
            ModulesAnalytics.Producer(context),
        )
    }
}
