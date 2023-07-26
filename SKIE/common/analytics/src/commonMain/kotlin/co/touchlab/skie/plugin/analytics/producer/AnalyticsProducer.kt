package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.configuration.SkieFeature

interface AnalyticsProducer {

    val name: String

    val feature: SkieFeature

    fun produce(): String
}
