@file:OptIn(ExperimentalKotest::class)

package co.touchlab.skie.plugin.libraries

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit


class CompareResultsTest: FunSpec({
    val onlyIndices = System.getenv("onlyIndices")?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.map { it.toInt() }?.toSet()
    val pureDir = File(System.getProperty("pureTestDir"))
    val skieDir = File(System.getProperty("skieTestDir"))
    val summary = GitHubSummary.create()
    val report = ComparisonReport.create(pureDir, skieDir, onlyIndices)

    CsvReporter.create(summary)?.write(report)

    context("Libraries") {
        report.missingInPure.forEach { library ->
            test(library.name) {
                fail { "Missing library in pure: $library, skie was ${report.skie.getValue(library).result}" }
            }
        }

        report.missingInSkie.forEach { library ->
            test(library.name) {
                fail { "Missing library in SKIE: $library, pure was ${report.pure.getValue(library).result}" }
            }
        }

        report.comparisons.forEach { comparison ->
            val (library, pure, skie) = comparison
            test(library.name) {
                when {
                    comparison.wasSkieSlower -> {
                        println("Skie was slower than pure for $library by ${comparison.absoluteAbsoluteDifferenceInSeconds}")
                    }

                    comparison.wasSkieFaster -> {
                        println("Skie was faster than pure for $library by ${comparison.absoluteAbsoluteDifferenceInSeconds}")
                    }

                    else -> {
                        println("Skie was as fast as pure for $library")
                    }
                }

                skie.result shouldBe pure.result
            }
        }
    }

    test("Reporting") {
        val table = MarkdownTableBuilder.statisticsTableHeader(report)

        summary.appendSection("## Stats") {
            +table.build()
        }

        val (pureFails, skieFails) = report.partitionedFails()

        summary.appendSection("## Notable comparisons") {
            report.notableComparisons.forEach { notableComparison ->
                val (comparison, reasons) = notableComparison
                val (library, pure, skie) = comparison
                +"- ${library.name}"
                reasons.forEach { reason ->
                    +"  - $reason"
                }
                +"  - Pure: ${pure.duration.toSecondsString()}, SKIE: ${skie.duration.toSecondsString()} (${comparison.absoluteDifference.toSecondsString()}, ${comparison.relativeDifference.toPercentString()})"
                +"  - Pure: ${pure.result}, SKIE: ${skie.result}"
            }
        }

        summary.appendSection("## Pure-only failures (${pureFails.size})") {
            pureFails.forEach { comparison ->
                +"- ${comparison.library}"
            }
        }
        summary.appendSection("## SKIE-only failures (${skieFails.size})") {
            skieFails.forEach { comparison ->
                +"- ${comparison.library}"
            }
        }
    }
})
