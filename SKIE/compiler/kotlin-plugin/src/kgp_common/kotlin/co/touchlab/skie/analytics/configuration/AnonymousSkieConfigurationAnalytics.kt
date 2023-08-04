package co.touchlab.skie.analytics.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable

@Serializable
data class AnonymousSkieConfigurationAnalytics(
    val enabledConfigurationFlags: Set<SkieConfigurationFlag>,
) {

    class Producer(private val skieConfiguration: SkieConfiguration) : AnalyticsProducer {

        override val name: String = "anonymous-skie-configuration"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Anonymous_SkieConfiguration

        override fun produce(): String =
            AnonymousSkieConfigurationAnalytics(skieConfiguration.enabledConfigurationFlags).toPrettyJson()
    }
}
