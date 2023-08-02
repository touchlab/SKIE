package co.touchlab.skie.analytics.performance

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.util.toPrettyJson
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object AnonymousSkiePerformanceAnalytics {

    class Producer(
        private val skieConfiguration: SkieConfiguration,
    ) : AnalyticsProducer {

        override val name: String = "anonymous-skie-performance"

        // Name : Time in seconds
        private val entries = mutableMapOf<String, Double>()

        override val feature: SkieFeature = SkieFeature.Analytics_Anonymous_SkiePerformance

        override fun produce(): String =
            entries.toPrettyJson()

        @OptIn(ExperimentalTime::class)
        fun <T> log(name: String, block: () -> T): T {
            val timedValue = measureTimedValue {
                block()
            }

            entries[name] = timedValue.duration.toDouble(DurationUnit.SECONDS)

            printLogIfEnabled(name, timedValue.duration)

            return timedValue.value
        }

        private fun printLogIfEnabled(name: String, duration: Duration) {
            if (SkieFeature.Debug_PrintSkiePerformanceLogs in skieConfiguration.enabledFeatures) {
                println("$name: ${duration.toDouble(DurationUnit.SECONDS)}s")
            }
        }
    }
}
