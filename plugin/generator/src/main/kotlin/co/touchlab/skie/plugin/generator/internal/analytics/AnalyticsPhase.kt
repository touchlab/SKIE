package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.analytics.air.AirAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.configuration.SkieConfigurationAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class AnalyticsPhase(
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        val skieConfigurationAnalyticsProducer = SkieConfigurationAnalyticsProducer(skieContext.configuration)

        skieContext.analyticsCollector.collect(skieConfigurationAnalyticsProducer)
    }

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        val airAnalyticsProducer = AirAnalyticsProducer(descriptorProvider, allModules.values.toList())

        skieContext.analyticsCollector.collect(airAnalyticsProducer)
    }
}
