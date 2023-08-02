package co.touchlab.skie.analytics

import co.touchlab.skie.analytics.compiler.common.AnonymousCommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.common.IdentifyingCommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.specific.AnonymousSpecificCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.AnonymousSkieConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.IdentifyingSkieConfigurationAnalytics
import co.touchlab.skie.analytics.environment.AnonymousCompilerEnvironmentAnalytics
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.konan.KonanConfig

internal class AnalyticsPhase(
    private val config: KonanConfig,
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        skieContext.analyticsCollector.collectAsync(
            AnonymousCommonCompilerConfigurationAnalytics.Producer(config),
            IdentifyingCommonCompilerConfigurationAnalytics.Producer(config),
            AnonymousSpecificCompilerConfigurationAnalytics.Producer(config),
            AnonymousSkieConfigurationAnalytics.Producer(skieContext.skieConfiguration),
            IdentifyingSkieConfigurationAnalytics.Producer(skieContext.skieConfiguration),
            AnonymousCompilerEnvironmentAnalytics.Producer(config),
        )
    }
}
