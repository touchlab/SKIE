package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object SkiePerformanceAnalytics {

    class Producer(
        private val skieConfiguration: SkieConfiguration,
    ) : AnalyticsProducer {

        override val name: String = "skie-performance"

        // Name : Time in seconds
        private val entries = mutableMapOf<String, Double>()

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkiePerformance

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
            if (SkieConfigurationFlag.Debug_PrintSkiePerformanceLogs in skieConfiguration.enabledConfigurationFlags) {
                println("$name: ${duration.toDouble(DurationUnit.SECONDS)}s")
            }
        }
    }
}
