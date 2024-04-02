package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.toDuration

object SkiePerformanceAnalytics {

    class Producer(
        private val skieConfiguration: SkieConfiguration,
    ) : AnalyticsProducer {

        private val mutex = Mutex()

        override val name: String = "skie-performance"

        // Name : Time in seconds
        private val entries = mutableMapOf<String, Double>()

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkiePerformance

        override fun produce(): String =
            runBlocking {
                mutex.withLock {
                    val total = entries.values.sum()

                    logLocked("Total", total.toDuration(DurationUnit.SECONDS))

                    entries.toPrettyJson()
                }
            }

        suspend fun logSkipped(name: String) {
            mutex.withLock {
                printLogIfEnabled("$name: Skipped")
            }
        }

        inline fun <T> logBlocking(name: String, crossinline block: () -> T): T =
            runBlocking {
                log(name, block)
            }

        @OptIn(ExperimentalTime::class)
        suspend inline fun <T> log(name: String, block: () -> T): T {
            val timedValue = measureTimedValue {
                block()
            }

            log(name, timedValue.duration)

            return timedValue.value
        }

        fun logBlocking(name: String, duration: Duration) {
            runBlocking {
                log(name, duration)
            }
        }

        suspend fun log(name: String, duration: Duration) {
            mutex.withLock {
                logLocked(name, duration)
            }
        }

        private fun logLocked(name: String, duration: Duration) {
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
