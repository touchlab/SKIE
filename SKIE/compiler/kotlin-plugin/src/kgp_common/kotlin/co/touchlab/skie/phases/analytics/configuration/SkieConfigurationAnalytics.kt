package co.touchlab.skie.phases.analytics.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.util.hash.hashed
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import kotlinx.serialization.Serializable

@Serializable
data class SkieConfigurationAnalytics(
    val groups: List<SkieConfiguration.Group>,
    val enabledConfigurationFlags: Set<SkieConfigurationFlag>,
) {

    class Producer(private val skieConfiguration: SkieConfiguration) : AnalyticsProducer {

        override val name: String = "skie-configuration"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkieConfiguration

        override fun produce(): String =
            SkieConfigurationAnalytics(
                groups = skieConfiguration.groups.map { it.anonymized() },
                enabledConfigurationFlags = skieConfiguration.enabledConfigurationFlags,
            ).toPrettyJson()
    }
}

private fun SkieConfiguration.Group.anonymized(): SkieConfiguration.Group =
    copy(target = target.hashed())
