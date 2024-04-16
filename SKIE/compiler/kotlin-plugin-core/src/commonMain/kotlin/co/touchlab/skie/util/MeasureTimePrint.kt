package co.touchlab.skie.util

import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
inline fun <T> measureTimePrint(name: String, block: () -> T): T {
    val (value, duration) = measureTimedValue {
        block()
    }

    val durationInSecondsAsString = String.format("%.6f", duration.toDouble(DurationUnit.SECONDS))

    println("$name: ${durationInSecondsAsString}s")

    return value
}
