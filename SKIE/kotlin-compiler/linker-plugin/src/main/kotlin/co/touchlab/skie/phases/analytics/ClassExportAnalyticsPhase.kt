package co.touchlab.skie.phases.analytics

import co.touchlab.skie.analytics.compiler.common.CommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.specific.SpecificCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.SkieConfigurationAnalytics
import co.touchlab.skie.analytics.environment.CompilerEnvironmentAnalytics
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.konanConfig

object ClassExportAnalyticsPhase : ClassExportPhase {

    context(context: ClassExportPhase.Context)
    override suspend fun execute() {
        context.analyticsCollector.collectAsync(
            CommonCompilerConfigurationAnalytics.Producer(context.konanConfig),
            SpecificCompilerConfigurationAnalytics.Producer(context.konanConfig),
            SkieConfigurationAnalytics.Producer(context.skieConfigurationData),
            CompilerEnvironmentAnalytics.Producer(context.konanConfig),
        )
    }
}
