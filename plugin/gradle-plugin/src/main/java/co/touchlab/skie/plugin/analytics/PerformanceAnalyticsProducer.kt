package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.performance.PerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration

internal class PerformanceAnalyticsProducer(private val linkTaskDuration: Duration) : AnalyticsProducer {

    override fun produce(): AnalyticsProducer.Result = AnalyticsProducer.Result(
        name = "performance",
        data = PerformanceAnalytics(
            linkTaskDurationInSeconds = linkTaskDuration.inWholeMilliseconds / 1000.0,
        ).encode()
    )
}

private fun PerformanceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()