package co.touchlab.skie.plugin.analytics.configuration

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsConfiguration(
    private val features: List<AnalyticsFeature>,
) {

    constructor(vararg features: AnalyticsFeature) : this(features.toList())

    fun getFeature(target: AnalyticsConfigurationTarget<*>): AnalyticsFeature? =
        features.lastOrNull { target.featureType.isInstance(it) }

    operator fun plus(other: AnalyticsConfiguration) : AnalyticsConfiguration =
        AnalyticsConfiguration(features + other.features)
}
