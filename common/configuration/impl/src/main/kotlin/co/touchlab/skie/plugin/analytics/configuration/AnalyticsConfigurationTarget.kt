package co.touchlab.skie.plugin.analytics.configuration

import kotlin.reflect.KClass

interface AnalyticsConfigurationTarget<T : AnalyticsFeature> {

    val featureType: KClass<T>
}
