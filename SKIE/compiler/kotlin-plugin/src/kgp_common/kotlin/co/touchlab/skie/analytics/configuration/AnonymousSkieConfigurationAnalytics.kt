package co.touchlab.skie.analytics.configuration

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable

@Serializable
data class AnonymousSkieConfigurationAnalytics(
    val enabledFeatures: Set<SkieFeature>,
) {

    class Producer(private val skieConfiguration: SkieConfiguration) : AnalyticsProducer {

        override val name: String = "anonymous-skie-configuration"

        override val feature: SkieFeature = SkieFeature.Analytics_Anonymous_SkieConfiguration

        override fun produce(): String =
            AnonymousSkieConfigurationAnalytics(skieConfiguration.enabledFeatures).toPrettyJson()
    }
}
