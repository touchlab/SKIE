package co.touchlab.skie.analytics.configuration

import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer

class SkieConfigurationAnalyticsProducer(
    private val skieConfiguration: SkieConfiguration,
) : AnalyticsProducer {

    override val name: String = "skie-configuration"

    override val feature: SkieFeature = SkieFeature.Analytics_SkieConfiguration

    override fun produce(): String =
        skieConfiguration.serialize()
}
