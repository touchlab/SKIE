package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature

interface AnalyticsProducer {

    val name: String

    val feature: SkieFeature

    fun produce(): String
}
