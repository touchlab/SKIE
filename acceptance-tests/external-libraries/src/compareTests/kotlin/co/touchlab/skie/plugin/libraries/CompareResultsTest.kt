@file:OptIn(ExperimentalKotest::class)

package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TestResult
import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.test.logging.info
import io.kotest.engine.test.logging.warn
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GitHubSummary(
    private val summaryFile: File?,
) {
    fun appendLine(message: String) {
        summaryFile?.appendText(message + "\n")
        println(message)
    }

    companion object {
        fun createIfAvailable(): GitHubSummary {
            return System.getenv("GITHUB_STEP_SUMMARY")
                ?.let(::File)
                ?.takeIf { it.exists() }
                .let(::GitHubSummary)
        }
    }
}


class CompareResultsTest: FunSpec({
    val onlyIndices = System.getenv("onlyIndices")?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.map { it.toInt() }?.toSet()
    val pureDir = File(System.getProperty("pureTestDir"))
    val skieDir = File(System.getProperty("skieTestDir"))
    val summary = GitHubSummary.createIfAvailable()

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

    System.getProperty("csvOutput")?.let { csvOutputPath ->
        val csvOutput = File(csvOutputPath)
        csvOutput.parentFile.mkdirs()
        csvOutput.writeText(
            libraries.joinToString(separator = "\n", prefix = "Index, Library, Pure Result, Pure Duration (s), SKIE Result, SKIE Duration (s)\n") {
                listOf(
                    it.library.index,
                    it.library.name,
                    it.pure.result.name,
                    it.pure.duration.toDouble(DurationUnit.SECONDS),
                    it.skie.result.name,
                    it.skie.duration.toDouble(DurationUnit.SECONDS),
                ).joinToString()
            }
        )
        summary.appendLine("\nNOTE: CSV output written to $csvOutputPath\n")
    }

    context("Libraries") {
        // Make sure all libraries are present in both
        val missingInPure = skieLibraries.keys - pureLibraries.keys
        val missingInSkie = pureLibraries.keys - skieLibraries.keys

        missingInPure.forEach { library ->
            test(library.name) {
                fail { "Missing library in pure: $library, skie was ${skieLibraries.getValue(library).result}" }
            }
        }

        missingInSkie.forEach { library ->
            test(library.name) {
                fail { "Missing library in SKIE: $library, pure was ${pureLibraries.getValue(library).result}" }
            }
        }

        libraries.forEach { comparison ->
            val (library, pure, skie) = comparison
            test(library.name) {
                when {
                    pure.duration < skie.duration -> {
                        println("Skie was slower than pure for $library by ${differenceInSeconds(skie.duration, pure.duration)}")
                    }

                    pure.duration > skie.duration -> {
                        println("Skie was faster than pure for $library by ${differenceInSeconds(skie.duration, pure.duration)}")
                    }

                    pure.duration == skie.duration -> {
                        println("Skie was as fast as pure for $library")
                    }
                }

                skie.result shouldBe pure.result
            }
        }
    }

    test("Total time") {
        val pureSummary = StatisticalSummary.from(libraries.map { it.pure.duration })
        val skieSummary = StatisticalSummary.from(libraries.map { it.skie.duration })
        val absoluteDifferenceSummary = StatisticalSummary.from(libraries.map { it.skie.duration - it.pure.duration })
        val relativeDifferenceSummary = StatisticalSummary.from(libraries.map { (it.skie.duration - it.pure.duration) / it.skie.duration })

        val table = MarkdownTableBuilder.statisticsTableHeader()
            .addSummaryRow("SKIE disabled", pureSummary) { it.toString(DurationUnit.SECONDS, decimals = 2) }
            .addSummaryRow("SKIE enabled", skieSummary) { it.toString(DurationUnit.SECONDS, decimals = 2) }
            .addSummaryRow("Absolute difference", absoluteDifferenceSummary) { it.toString(DurationUnit.SECONDS, decimals = 2) }
            .addSummaryRow("Relative difference", relativeDifferenceSummary) { "${(it * 100).roundToInt()}%" }


        summary.appendLine("### Stats")
        summary.appendLine("")
        summary.appendLine(table.build())
        summary.appendLine("")
    }

    val (pureFails, skieFails) = libraries
        .filter { it.pure.result != it.skie.result }
        .partition { it.skie.result == Comparison.Result.Success }

    test("Failures") {
        summary.appendLine("### Pure-only failures (${pureFails.size}):")
        pureFails.forEach { comparison ->
            summary.appendLine("- ${comparison.library}")
        }
        summary.appendLine("### SKIE-only failures (${skieFails.size}):")
        skieFails.forEach { comparison ->
            summary.appendLine("- ${comparison.library}")
        }
    }
})


// function that takes list of lists and produces a Markdown table


private class MarkdownTableBuilder private constructor(
    private val header: List<String>,
) {

    private val rows = mutableListOf<List<String>>()

    fun addRow(vararg cells: String): MarkdownTableBuilder {
        rows.add(cells.take(header.size))
        return this
    }

    fun <T: Comparable<T>> addSummaryRow(name: String, summary: StatisticalSummary<T>, toString: (T) -> String): MarkdownTableBuilder {
        return addRow(
            name,
            toString(summary.min),
            toString(summary.max),
            toString(summary.total),
            toString(summary.average),
            toString(summary.median),
            toString(summary.standardDeviation),
        )
    }

    companion object {
        fun statisticsTableHeader() = MarkdownTableBuilder(
            listOf(
                "Statistic",
                "Min",
                "Max",
                "Total",
                "Average",
                "Median",
                "Standard Deviation",
            )
        )
    }

    fun build(): String {
        val columnWidths = header.map { it.length }.toMutableList()
        rows.forEach {  row ->
            row.forEachIndexed { index, cell ->
                columnWidths[index] = max(columnWidths[index], cell.length)
            }
        }
        val paddedHeader = header.mapIndexed { index, cell ->
            cell.padEnd(columnWidths[index])
        }
        val paddedRows = rows.map { row ->
            row.mapIndexed { index, cell ->
                cell.padEnd(columnWidths[index])
            }
        }
        val headerSeparator = columnWidths.map { columnWidth ->
            "-".repeat(columnWidth)
        }

        val tableContents = listOf(paddedHeader, headerSeparator) + paddedRows
        return tableContents.joinToString("\n") { row ->
            row.joinToString(" | ", prefix = "| ", postfix = " |")
        }
    }
}

private class StatisticalSummary<T: Comparable<T>>(
    val min: T,
    val max: T,
    val total: T,
    val average: T,
    val median: T,
    val standardDeviation: T,
) {
    fun <U: Comparable<U>> convert(converter: (T) -> U): StatisticalSummary<U> {
        return StatisticalSummary(
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
        fun from(unsortedDurations: List<Duration>): StatisticalSummary<Duration> {
            return from(unsortedDurations.map { it.toDouble(DurationUnit.MILLISECONDS) })
                .convert { it.toDuration(DurationUnit.MILLISECONDS) }
        }

        @JvmName("fromDoubles")
        fun from(unsortedDoubles: List<Double>): StatisticalSummary<Double> {
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

            return StatisticalSummary(
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

object KotestProjectConfig: AbstractProjectConfig() {
    override val logLevel: LogLevel? = LogLevel.Info
}

private data class Comparison(
    val library: Library,
    val pure: Item,
    val skie: Item,
) {
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

private fun differenceInSeconds(a: Duration, b: Duration): String {
    return if (a > b) {
        a - b
    } else {
        b - a
    }.toString(DurationUnit.SECONDS, decimals = 2)
}
