package co.touchlab.skie.plugin.api.analytics

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.analytics.performance.SkiePerformanceAnalytics
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit
import kotlinx.serialization.encodeToString

class SkiePerformanceAnalyticsProducer : AnalyticsProducer<AnalyticsFeature.SkiePerformance> {

    override val featureType: KClass<AnalyticsFeature.SkiePerformance> = AnalyticsFeature.SkiePerformance::class

    override val name: String = "skie-performance"

    private val entries = mutableListOf<SkiePerformanceAnalytics.Entry>()

    @OptIn(ExperimentalTime::class)
    fun <T> log(name: String, block: () -> T): T {
        val timedValue = measureTimedValue {
            block()
        }

        entries.add(
            SkiePerformanceAnalytics.Entry(
                name = name,
                timeInSeconds = timedValue.duration.toDouble(DurationUnit.SECONDS),
            )
        )

        return timedValue.value
    }

    override fun produce(configuration: AnalyticsFeature.SkiePerformance): ByteArray =
        SkiePerformanceAnalytics(entries).encode()
}

private fun SkiePerformanceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()
