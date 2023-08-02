package co.touchlab.skie.analytics.configuration

import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable

@Serializable
data class IdentifyingSkieConfigurationAnalytics(
    val groups: List<SkieConfiguration.Group>,
) {

    class Producer(private val skieConfiguration: SkieConfiguration) : AnalyticsProducer {

        override val name: String = "identifying-skie-configuration"

        override val feature: SkieFeature = SkieFeature.Analytics_Identifying_SkieConfiguration

        override fun produce(): String =
            IdentifyingSkieConfigurationAnalytics(skieConfiguration.groups).toPrettyJson()
    }
}
