package co.touchlab.skie.plugin.api.analytics

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Environment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

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
            ),
        )

        printLogInDevEnvironment(name, timedValue.duration)

        return timedValue.value
    }

    private fun printLogInDevEnvironment(name: String, duration: Duration) {
        if (Environment.current == Environment.Dev) {
            println("$name: ${duration.toDouble(DurationUnit.SECONDS)}s")
        }
    }

    override fun produce(configuration: AnalyticsFeature.SkiePerformance): ByteArray =
        SkiePerformanceAnalytics(entries).encode()
}

private fun SkiePerformanceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()
