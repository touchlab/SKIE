package co.touchlab.skie.plugin.analytics.performance

import kotlinx.serialization.Serializable

@Serializable
data class GradlePerformanceAnalytics(
    val linkTaskDurationInSeconds: Double,
)
