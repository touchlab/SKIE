package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfigurationTarget
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature

interface AnalyticsProducer<T : AnalyticsFeature> : AnalyticsConfigurationTarget<T> {

    val name: String

    fun produce(configuration: T): ByteArray
}
