package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.analytics.producer.AnalyticsEncryptor
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import kotlinx.serialization.json.Json

internal class AirAnalyticsPhase(
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val json = Json { classDiscriminator = "jsonType" }

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        val air = AirAnalyticsCollector(descriptorProvider).collectAnalytics(allModules.values.toList())
    }
}
