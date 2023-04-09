package co.touchlab.skie.plugin.analytics.performance

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceAnalytics(
    val linkTaskDurationInSeconds: Double,
)
