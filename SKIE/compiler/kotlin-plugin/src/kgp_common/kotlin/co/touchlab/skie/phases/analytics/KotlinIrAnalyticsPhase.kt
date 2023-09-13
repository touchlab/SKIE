package co.touchlab.skie.phases.analytics

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.analytics.modules.ModulesAnalytics

object KotlinIrAnalyticsPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override fun execute() {
        analyticsCollector.collectSynchronously(
            ModulesAnalytics.Producer(context),
        )
    }
}
