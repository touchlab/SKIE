package co.touchlab.skie.plugin.api.analytics

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class SkiePerformanceAnalyticsProducer(
    private val skieConfiguration: SkieConfiguration,
) : AnalyticsProducer {

    override val name: String = "skie-performance"

    private val entries = mutableListOf<SkiePerformanceAnalytics.Entry>()

    override val feature: SkieFeature = SkieFeature.Analytics_SkiePerformance

    override fun produce(): String =
        SkiePerformanceAnalytics(entries).serialize()

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

        printLogIfEnabled(name, timedValue.duration)

        return timedValue.value
    }

    private fun printLogIfEnabled(name: String, duration: Duration) {
        if (SkieFeature.Debug_PrintSkiePerformanceLogs in skieConfiguration.enabledFeatures) {
            println("$name: ${duration.toDouble(DurationUnit.SECONDS)}s")
        }
    }
}

// WIP one function for all analytics
private fun SkiePerformanceAnalytics.serialize(): String =
    Json.encodeToString(this)
