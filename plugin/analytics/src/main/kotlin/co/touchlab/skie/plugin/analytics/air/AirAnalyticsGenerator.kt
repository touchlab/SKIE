package co.touchlab.skie.plugin.analytics.air

import co.touchlab.skie.plugin.analytics.air.collector.AirAnalyticsCollector
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText

class AirAnalyticsGenerator {

    private val json = Json { classDiscriminator = "jsonType" }

    fun generateStatistics(projectName: String, irModules: List<IrModuleFragment>, descriptorProvider: DescriptorProvider) {
        val air = AirAnalyticsCollector(descriptorProvider).collectAnalytics(projectName, irModules)

    }
}
