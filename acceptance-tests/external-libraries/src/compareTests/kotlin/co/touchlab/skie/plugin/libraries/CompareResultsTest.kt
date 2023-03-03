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
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

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
    val onlyIndices = System.getenv("onlyIndices")?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.map { it.toInt() }?.toSet()?.takeIf { it.isNotEmpty() }
    val pureDir = File(System.getProperty("pureTestDir"))
    val skieDir = File(System.getProperty("skieTestDir"))
    val summary = GitHubSummary.createIfAvailable()

    val pureLibraries = pureDir.listFiles().orEmpty()
        .filter { onlyIndices == null || it.name.substringBefore('-').toInt() in onlyIndices }
        .associate {
            it.name to Comparison.Item(it)
        }
    val skieLibraries = skieDir.listFiles().orEmpty()
        .filter { onlyIndices == null || it.name.substringBefore('-').toInt() in onlyIndices }
        .associate {
            it.name to Comparison.Item(it)
        }

    // Zip pure and skie libraries together where same key exists
    val libraries = pureLibraries.keys.intersect(skieLibraries.keys)
        .sorted()
        .map { library ->
            Comparison(
                library = library,
                pure = pureLibraries.getValue(library),
                skie = skieLibraries.getValue(library),
            )
        }

    context("Libraries") {
        // Make sure all libraries are present in both
        val missingInPure = skieLibraries.keys - pureLibraries.keys
        val missingInSkie = pureLibraries.keys - skieLibraries.keys

        missingInPure.forEach { library ->
            test(library) {
                fail { "Missing library in pure: $library, skie was ${skieLibraries.getValue(library).result}" }
            }
        }

        missingInSkie.forEach { library ->
            test(library) {
                fail { "Missing library in SKIE: $library, pure was ${pureLibraries.getValue(library).result}" }
            }
        }

        libraries.forEach { comparison ->
            val (library, pure, skie) = comparison
            test(library) {
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
        val (totalPureTime, totalSkieTime) = libraries.fold(Duration.ZERO) { accumulator, comparison ->
            accumulator + comparison.pure.duration
        } to libraries.fold(Duration.ZERO) { accumulator, comparison ->
            accumulator + comparison.skie.duration
        }
        summary.appendLine("### Stats")
        summary.appendLine("- Total pure time: ${totalPureTime.toString(DurationUnit.SECONDS, decimals = 2)}")
        summary.appendLine("- Total SKIE time: ${totalSkieTime.toString(DurationUnit.SECONDS, decimals = 2)}")
        summary.appendLine("- Total difference: ${differenceInSeconds(totalSkieTime, totalPureTime)}")

        val relativeDifference = ((totalSkieTime - totalPureTime) / totalPureTime).times(100).roundToInt()
        summary.appendLine("- Relative difference: **$relativeDifference%**")
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

object KotestProjectConfig: AbstractProjectConfig() {
    override val logLevel: LogLevel? = LogLevel.Info
}

private data class Comparison(
    val library: String,
    val pure: Item,
    val skie: Item,
) {
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
