package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.toDuration

object SkiePerformanceAnalytics {

    class Producer(
        private val skieConfiguration: SkieConfiguration,
    ) : AnalyticsProducer {

        override val name: String = "skie-performance"

        // Name : Time in seconds
        private val entries = mutableMapOf<String, Double>()

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkiePerformance

        override fun produce(): String {
            val total = entries.values.sum()

            log("Total", total.toDuration(DurationUnit.SECONDS))

            return entries.toPrettyJson()
        }

        fun logSkipped(name: String) {
            printLogIfEnabled("$name: Skipped")
        }

        @OptIn(ExperimentalTime::class)
        inline fun <T> log(name: String, block: () -> T): T {
            val timedValue = measureTimedValue {
                block()
            }

            log(name, timedValue.duration)

            return timedValue.value
        }

        fun log(name: String, duration: Duration) {
            entries[name] = duration.toDouble(DurationUnit.SECONDS)

            printFormattedLogIfEnabled(name, duration)
        }

        private fun printFormattedLogIfEnabled(name: String, duration: Duration) {
            val durationInSeconds = duration.toDouble(DurationUnit.SECONDS)

            val durationInSecondsAsString = String.format("%.6f", durationInSeconds)

            printLogIfEnabled("$name: ${durationInSecondsAsString}s")
        }

        private fun printLogIfEnabled(content: String) {
            if (SkieConfigurationFlag.Debug_PrintSkiePerformanceLogs in skieConfiguration.enabledConfigurationFlags) {
                println(content)
            }
        }
    }
}
