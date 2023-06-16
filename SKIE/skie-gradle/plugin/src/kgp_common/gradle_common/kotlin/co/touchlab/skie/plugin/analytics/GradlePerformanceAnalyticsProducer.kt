package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.performance.GradlePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import kotlin.reflect.KClass

internal class GradlePerformanceAnalyticsProducer(
    private val linkTaskDuration: Duration,
) : AnalyticsProducer<AnalyticsFeature.GradlePerformance> {

    override val featureType: KClass<AnalyticsFeature.GradlePerformance> = AnalyticsFeature.GradlePerformance::class

    override val name: String = "gradle-performance"

    override fun produce(configuration: AnalyticsFeature.GradlePerformance): ByteArray =
        GradlePerformanceAnalytics(
            linkTaskDurationInSeconds = linkTaskDuration.toMillis().toDouble() / 1000.0,
        ).encode()
}

private fun GradlePerformanceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()
