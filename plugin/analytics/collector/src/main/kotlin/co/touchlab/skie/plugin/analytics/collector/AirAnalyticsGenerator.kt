package co.touchlab.skie.plugin.analytics.collector

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AirAnalyticsGenerator {

    private val json = Json { classDiscriminator = "jsonType" }

    fun generateStatistics(projectName: String, irModules: List<IrModuleFragment>, descriptorProvider: DescriptorProvider) {
        val air = AirAnalyticsCollector(descriptorProvider).collectAnalytics(projectName, irModules)
    }
}
