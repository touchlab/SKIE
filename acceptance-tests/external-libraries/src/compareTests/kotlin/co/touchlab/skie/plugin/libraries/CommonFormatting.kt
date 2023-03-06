package co.touchlab.skie.plugin.libraries

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun Double.toPercentString(): String {
    return "${(this * 100).roundToInt()}%"
}

fun Duration.toSecondsString(): String {
    return toString(DurationUnit.SECONDS, decimals = 2)
}
