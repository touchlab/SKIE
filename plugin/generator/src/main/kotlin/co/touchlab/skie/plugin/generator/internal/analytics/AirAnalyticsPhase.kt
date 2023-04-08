package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class AirAnalyticsPhase(
    private val descriptorProvider: DescriptorProvider,
    private val analyticsCollector: AnalyticsCollector,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        val airAnalyticsProducer = AirAnalyticsProducer(descriptorProvider, allModules.values.toList())

        analyticsCollector.collect(airAnalyticsProducer)
    }
}
