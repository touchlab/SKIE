package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieConfigurationFlag

interface AnalyticsProducer {

    val name: String

    val configurationFlag: SkieConfigurationFlag

    fun produce(): String
}
