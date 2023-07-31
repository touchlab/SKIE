package co.touchlab.skie.analytics

import co.touchlab.skie.analytics.configuration.SkieConfigurationAnalyticsProducer
import co.touchlab.skie.debug.compiler.CompilerDebugProducer
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
        skieContext.analyticsCollector.collect(
            CompilerDebugProducer(config),
            SkieConfigurationAnalyticsProducer(skieContext.skieConfiguration),
        )
    }
}
