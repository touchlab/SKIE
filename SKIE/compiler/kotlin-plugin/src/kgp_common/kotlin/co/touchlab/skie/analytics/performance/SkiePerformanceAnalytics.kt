package co.touchlab.skie.analytics.performance

import kotlinx.serialization.Serializable

@Serializable
data class SkiePerformanceAnalytics(
    val entries: List<Entry>,
) {

    @Serializable
    data class Entry(
        val name: String,
        val timeInSeconds: Double,
    )
}
