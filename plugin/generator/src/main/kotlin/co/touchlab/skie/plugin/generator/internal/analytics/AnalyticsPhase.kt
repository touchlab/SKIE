package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.analytics.collector.AirAnalyticsGenerator
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class AnalyticsPhase(
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        // TODO Project name
        AirAnalyticsGenerator().generateStatistics("TODO", allModules.values.toList(), descriptorProvider)
    }
}
