package co.touchlab.skie.phases.analytics.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.hash.hashed
import kotlinx.serialization.Serializable

@Serializable
data class SkieConfigurationAnalytics(
    val groups: List<CompilerSkieConfigurationData.Group>,
    val enabledConfigurationFlags: Set<SkieConfigurationFlag>,
) {

    class Producer(private val skieConfigurationData: CompilerSkieConfigurationData) : AnalyticsProducer {

        override val name: String = "skie-configuration"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkieConfiguration

        override fun produce(): String =
            SkieConfigurationAnalytics(
                groups = skieConfigurationData.groups.map { it.anonymized() },
                enabledConfigurationFlags = skieConfigurationData.enabledConfigurationFlags,
            ).toPrettyJson()
    }
}

private fun CompilerSkieConfigurationData.Group.anonymized(): CompilerSkieConfigurationData.Group =
    copy(target = target.hashed())
