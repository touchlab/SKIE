package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfigurationTarget

interface AnalyticsProducer<T : AnalyticsFeature> : AnalyticsConfigurationTarget<T> {

    val name: String

    fun produce(configuration: T): ByteArray
}
