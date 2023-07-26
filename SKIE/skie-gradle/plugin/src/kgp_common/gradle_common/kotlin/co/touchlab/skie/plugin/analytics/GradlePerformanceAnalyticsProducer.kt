package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.performance.GradlePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration

internal class GradlePerformanceAnalyticsProducer(
    private val linkTaskDuration: Duration,
) : AnalyticsProducer {

    override val name: String = "gradle-performance"

    override val feature: SkieFeature = SkieFeature.Analytics_GradlePerformance

    override fun produce(): String =
        GradlePerformanceAnalytics(
            linkTaskDurationInSeconds = linkTaskDuration.toMillis().toDouble() / 1000.0,
        ).serialize()
}

private fun GradlePerformanceAnalytics.serialize(): String =
    Json.encodeToString(this)
