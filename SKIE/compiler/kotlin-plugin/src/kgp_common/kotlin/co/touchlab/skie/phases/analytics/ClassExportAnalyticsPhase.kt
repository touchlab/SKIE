package co.touchlab.skie.phases.analytics

import co.touchlab.skie.analytics.compiler.common.CommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.specific.SpecificCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.SkieConfigurationAnalytics
import co.touchlab.skie.analytics.environment.CompilerEnvironmentAnalytics
import co.touchlab.skie.phases.ClassExportCompilerPhase

object ClassExportAnalyticsPhase : ClassExportCompilerPhase {

    context(ClassExportCompilerPhase.Context)
    override suspend fun execute() {
        analyticsCollector.collectAsync(
            CommonCompilerConfigurationAnalytics.Producer(konanConfig),
            SpecificCompilerConfigurationAnalytics.Producer(konanConfig),
            SkieConfigurationAnalytics.Producer(skieConfigurationData),
            CompilerEnvironmentAnalytics.Producer(konanConfig),
        )
    }
}
