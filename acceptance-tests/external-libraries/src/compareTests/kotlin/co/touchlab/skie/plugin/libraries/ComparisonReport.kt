package co.touchlab.skie.plugin.libraries

import io.kotest.matchers.doubles.percent
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class ComparisonReport(
    val comparisons: List<Comparison>,
    val pure: Map<Comparison.Library, Comparison.Item>,
    val skie: Map<Comparison.Library, Comparison.Item>,
    val missingInPure: Set<Comparison.Library>,
    val missingInSkie: Set<Comparison.Library>,
    val failedTests: FailedTests,
    val stats: Stats,
) {
    val notableComparisons: List<NotableComparison> = comparisons.mapNotNull {
        stats.notable(it)
    }

    fun partitionedFails(): Pair<List<Comparison>, List<Comparison>> {
        return comparisons
            .filter { it.pure.result != it.skie.result }
            .partition {
                it.pure.result == Comparison.Result.Failure
            }
    }

    companion object {
        fun create(pureDir: File, skieDir: File, onlyIndices: Set<Int>?): ComparisonReport {
            val pureLibraries = pureDir.listFiles().orEmpty()
                .associateBy(Comparison::Library)
                .filterKeys { onlyIndices.isNullOrEmpty() || it.index in onlyIndices }
                .mapValues { Comparison.Item(it.value) }
            val skieLibraries = skieDir.listFiles().orEmpty()
                .associateBy(Comparison::Library)
                .filterKeys { onlyIndices.isNullOrEmpty() || it.index in onlyIndices }
                .mapValues { Comparison.Item(it.value) }

            // Zip pure and skie libraries together where same key exists
            val libraries = pureLibraries.keys.intersect(skieLibraries.keys)
                .sortedBy { it.name }
                .map { library ->
                    Comparison(
                        library = library,
                        pure = pureLibraries.getValue(library),
                        skie = skieLibraries.getValue(library),
                    )
                }

            return ComparisonReport(
                comparisons = libraries,
                pure = pureLibraries,
                skie = skieLibraries,
                missingInPure = skieLibraries.keys - pureLibraries.keys,
                missingInSkie = pureLibraries.keys - skieLibraries.keys,
                failedTests = FailedTests.from(libraries),
                stats = Stats.from(libraries),
            )
        }
    }

    data class FailedTests(
        val both: List<Comparison>,
        val onlyPure: List<Comparison>,
        val onlySkie: List<Comparison>,
    ) {
        companion object {
            fun from(comparisons: List<Comparison>): FailedTests {
                val both = mutableListOf<Comparison>()
                val onlyPure = mutableListOf<Comparison>()
                val onlySkie = mutableListOf<Comparison>()

                comparisons.forEach {
                    when {
                        it.pure.result == Comparison.Result.Failure && it.skie.result == Comparison.Result.Failure -> both.add(it)
                        it.pure.result == Comparison.Result.Failure -> onlyPure.add(it)
                        it.skie.result == Comparison.Result.Failure -> onlySkie.add(it)
                    }
                }

                return FailedTests(
                    both = both,
                    onlyPure = onlyPure,
                    onlySkie = onlySkie,
                )
            }
        }
    }

    data class NotableComparison(
        val comparison: Comparison,
        val reasons: List<String>,
    )

    data class Stats(
        val pure: Item<Duration>,
        val skie: Item<Duration>,
        val absoluteDifference: Item<Duration>,
        val relativeDifference: Item<Double>,
    ) {
        fun notable(comparison: Comparison): NotableComparison? {
            val reasons = listOfNotNull(
                if (absoluteDifference.isNotable(comparison.absoluteDifference)) {
                    "Absolute difference ${comparison.absoluteDifference.toSecondsString()} is outside of ${absoluteDifference.notableRange}"
                } else null,
                if (relativeDifference.isNotable(comparison.relativeDifference)) {
                    "Relative difference ${comparison.relativeDifference.toPercentString()} is outside of ${relativeDifference.notableRange}"
                } else null,
            )

            return if (reasons.isNotEmpty()) {
                NotableComparison(
                    comparison = comparison,
                    reasons = reasons,
                )
            } else {
                null
            }
        }

        @get:JvmName("durationNotableRange")
        private val Item<Duration>.notableRange: String
            get() = "${average.toSecondsString()} ± ${standardDeviation.toSecondsString()}"

        @get:JvmName("doubleNotableRange")
        private val Item<Double>.notableRange: String
            get() = "${average.toPercentString()} ± ${standardDeviation.toPercentString()}"


        private fun Item<Duration>.isNotable(value: Duration): Boolean {
            return value > average + standardDeviation ||
                value < average - standardDeviation
        }

        private fun Item<Double>.isNotable(value: Double): Boolean {
            return value > average + standardDeviation ||
                value < average - standardDeviation
        }

        data class Item<T: Comparable<T>>(
            val min: T,
            val max: T,
            val total: T,
            val average: T,
            val median: T,
            val standardDeviation: T,
        ) {
            fun <U: Comparable<U>> convert(converter: (T) -> U): Item<U> {
                return Item(
                    min = converter(min),
                    max = converter(max),
                    total = converter(total),
                    average = converter(average),
                    median = converter(median),
                    standardDeviation = converter(standardDeviation),
                )
            }

            companion object {
                @JvmName("fromDurations")
                fun from(unsortedDurations: List<Duration>): Item<Duration> {
                    return from(unsortedDurations.map { it.toDouble(DurationUnit.MILLISECONDS) })
                        .convert { it.toDuration(DurationUnit.MILLISECONDS) }
                }

                @JvmName("fromDoubles")
                fun from(unsortedDoubles: List<Double>): Item<Double> {
                    check(unsortedDoubles.isNotEmpty()) { "Cannot create summary from empty list" }
                    val doubles = unsortedDoubles.sorted()

                    var min = Double.POSITIVE_INFINITY
                    var max = Double.NEGATIVE_INFINITY
                    var total = 0.0

                    doubles.forEach { value ->
                        min = minOf(min, value)
                        max = maxOf(max, value)
                        total += value
                    }

                    val mean = total / doubles.size

                    var totalStandardDeviation = 0.0
                    doubles.forEach { value ->
                        totalStandardDeviation += (value - mean).pow(2)
                    }
                    val standardDeviation = sqrt(totalStandardDeviation / doubles.size)

                    val median = if (doubles.size % 2 == 0) {
                        (doubles[doubles.size / 2] + doubles[doubles.size / 2 - 1]) / 2
                    } else {
                        doubles[doubles.size / 2]
                    }

                    return Item(
                        min = min,
                        max = max,
                        total = total,
                        average = mean,
                        median = median,
                        standardDeviation = standardDeviation,
                    )
                }
            }
        }

        companion object {
            fun from(libraries: List<Comparison>): Stats {
                return Stats(
                    pure = Item.from(libraries.map { it.pure.duration }),
                    skie = Item.from(libraries.map { it.skie.duration }),
                    absoluteDifference = Item.from(libraries.map { it.absoluteDifference }),
                    relativeDifference = Item.from(libraries.map { it.relativeDifference }),
                )
            }
        }
    }
}
