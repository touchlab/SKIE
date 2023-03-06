package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit

data class Comparison(
    val library: Library,
    val pure: Item,
    val skie: Item,
) {
    val absoluteDifference: Duration = skie.duration - pure.duration

    val relativeDifference: Double = absoluteDifference / pure.duration

    val wasSkieFaster: Boolean = absoluteDifference.isNegative()

    val wasSkieSlower: Boolean = absoluteDifference.isPositive()

    val absoluteAbsoluteDifferenceInSeconds: String = absoluteDifference.absoluteValue.toSecondsString()

    data class Library(
        val index: Int,
        val name: String,
    ) {
        constructor(libraryDir: File): this(
            index = libraryDir.name.substringBefore('-').toInt(),
            name = libraryDir.name.substringAfter('-'),
        )

        override fun toString(): String {
            return "[$index] $name"
        }
    }

    data class Item(
        val result: Result,
        val duration: Duration,
    ) {
        constructor(libraryDir: File): this(
            result = Result.fromString(libraryDir.resolve("result.txt").readText()),
            duration = Duration.parseIsoString(libraryDir.resolve("duration.txt").readText()),
        )
    }

    enum class Result {
        Success,
        Failure;

        companion object {
            fun fromString(string: String): Result {
                return when (string) {
                    ExpectedTestResult.SUCCESS -> Success
                    ExpectedTestResult.FAILURE -> Failure
                    else -> error("Unknown result: $string")
                }
            }
        }
    }
}
