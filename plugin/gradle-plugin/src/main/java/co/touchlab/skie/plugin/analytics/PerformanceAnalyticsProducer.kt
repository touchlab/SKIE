package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.performance.PerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import kotlin.reflect.KClass

internal class PerformanceAnalyticsProducer(
    private val linkTaskDuration: Duration,
) : AnalyticsProducer<AnalyticsFeature.Performance> {

    override val featureType: KClass<AnalyticsFeature.Performance> = AnalyticsFeature.Performance::class

    override val name: String = "performance"

    override fun produce(configuration: AnalyticsFeature.Performance): ByteArray =
        PerformanceAnalytics(
            linkTaskDurationInSeconds = linkTaskDuration.toMillis().toDouble() / 1000.0,
        ).encode()
}

private fun PerformanceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()
