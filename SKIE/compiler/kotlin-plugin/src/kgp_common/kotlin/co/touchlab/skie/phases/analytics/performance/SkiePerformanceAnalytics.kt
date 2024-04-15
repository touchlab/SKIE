package co.touchlab.skie.phases.analytics.performance

import co.touchlab.skie.configuration.RootConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.toDuration

object SkiePerformanceAnalytics {

    class Producer(
        private val rootConfiguration: RootConfiguration,
    ) : AnalyticsProducer {

        private val dispatcher: CoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        override val name: String = "skie-performance"

        private val entries = mutableListOf<Entry>()

        private var collected = false

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_SkiePerformance

        override fun produce(): String =
            runBlocking(dispatcher) {
                logTotal(Kind.Foreground)
                logTotal(Kind.Background)

                collected = true

                entries.toPrettyJson()
            }

        private fun logTotal(kind: Kind) {
            val totalInSeconds = entries.filter { it.kind == kind }.sumOf { it.duration.toDouble(DurationUnit.SECONDS) }

            val totalDuration = totalInSeconds.toDuration(DurationUnit.SECONDS)

            logLocked("Total", totalDuration, kind)
        }

        suspend fun logSkipped(name: String) {
            withContext(dispatcher) {
                printLogIfEnabled("$name: Skipped")
            }
        }

        inline fun <T> logBlocking(name: String, kind: Kind = Kind.Foreground, crossinline block: () -> T): T =
            runBlocking {
                log(name, kind, block)
            }

        @OptIn(ExperimentalTime::class)
        suspend inline fun <T> log(name: String, kind: Kind = Kind.Foreground, block: () -> T): T {
            val timedValue = measureTimedValue {
                block()
            }

            log(name, timedValue.duration, kind)

            return timedValue.value
        }

        fun logBlocking(name: String, duration: Duration, kind: Kind = Kind.Foreground) {
            runBlocking {
                log(name, duration, kind)
            }
        }

        suspend fun log(name: String, duration: Duration, kind: Kind = Kind.Foreground) {
            withContext(dispatcher) {
                logLocked(name, duration, kind)
            }
        }

        private fun logLocked(name: String, duration: Duration, kind: Kind = Kind.Foreground) {
            if (collected) {
                return
            }

            val entry = Entry(name, duration, kind)

            entries.add(entry)

            printFormattedLogIfEnabled(entry)
        }

        private fun printFormattedLogIfEnabled(entry: Entry) {
            val durationInSeconds = entry.duration.toDouble(DurationUnit.SECONDS)

            val durationInSecondsAsString = String.format("%.6f", durationInSeconds)

            val kindName = when (entry.kind) {
                Kind.Foreground -> "fg"
                Kind.Background -> "bg"
            }

            printLogIfEnabled("${entry.name}: ${durationInSecondsAsString}s [$kindName]")
        }

        private fun printLogIfEnabled(content: String) {
            if (collected) {
                return
            }

            if (SkieConfigurationFlag.Debug_PrintSkiePerformanceLogs in rootConfiguration.enabledFlags) {
                println(content)
            }
        }
    }

    @Serializable
    private data class Entry(
        val name: String,
        val duration: Duration,
        val kind: Kind,
    )

    enum class Kind {
        Foreground,
        Background,
    }
}
