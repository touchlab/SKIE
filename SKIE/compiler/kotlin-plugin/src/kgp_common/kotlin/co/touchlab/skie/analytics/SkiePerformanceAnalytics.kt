package co.touchlab.skie.analytics

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
