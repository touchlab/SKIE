package co.touchlab.skie.plugin.generator.internal.analytics.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer

class SkieConfigurationAnalyticsProducer(private val configuration: Configuration) : AnalyticsProducer {

    override fun produce(): AnalyticsProducer.Result = AnalyticsProducer.Result(
        name = "skie-configuration",
        data = configuration.serialize().toByteArray(),
    )
}
