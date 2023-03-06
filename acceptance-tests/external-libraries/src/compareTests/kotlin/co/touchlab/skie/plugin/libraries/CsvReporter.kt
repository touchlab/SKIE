package co.touchlab.skie.plugin.libraries

import java.io.File
import kotlin.math.abs
import kotlin.time.DurationUnit

class CsvReporter private constructor(
    private val summary: GitHubSummary,
    private val outputDir: File,
    private val runId: String?,
) {
    fun write(report: ComparisonReport) {
        outputDir.mkdirs()

        writeBaseReport(report.comparisons)
        writeAbsoluteDiffReport(report.comparisons)
        writeRelativeDiffReport(report.comparisons)

        summary.appendLine("\nNOTE: CSV reports written to $outputDir\n")
    }

    private fun writeBaseReport(libraries: List<Comparison>) {
        reportFile("sorted-by-library").writeReport(
            libraries.sortedBy { it.library.name }
        )
    }

    private fun writeAbsoluteDiffReport(libraries: List<Comparison>) {
        reportFile("sorted-by-absolute-diff").writeReport(
            libraries.sortedByDescending { it.absoluteDifference }
        )
    }

    private fun writeRelativeDiffReport(libraries: List<Comparison>) {
        reportFile("sorted-by-relative-diff").writeReport(
            libraries.sortedByDescending { it.relativeDifference }
        )
    }

    private fun reportFile(name: String) = outputDir.resolve(listOfNotNull(runId, name).joinToString("-", postfix = ".csv"))

    private fun File.writeReport(sortedLibraries: List<Comparison>) {
        this.writeText(
            sortedLibraries.joinToString(separator = "\n", prefix = "Index, Library, Pure Result, Pure Duration (s), SKIE Result, SKIE Duration (s), Absolute Difference (s), Relative Difference \n") {
                listOf(
                    it.library.index,
                    it.library.name,
                    it.pure.result.name,
                    it.pure.duration.toDouble(DurationUnit.SECONDS),
                    it.skie.result.name,
                    it.skie.duration.toDouble(DurationUnit.SECONDS),
                    it.absoluteDifference.toDouble(DurationUnit.SECONDS),
                    it.relativeDifference,
                ).joinToString()
            }
        )

    }

    companion object {
        fun create(summary: GitHubSummary): CsvReporter? {
            return CsvReporter(
                summary = summary,
                outputDir = File(System.getProperty("csvOutputDir") ?: return null),
                runId = System.getenv("GITHUB_RUN_ID")
            )
        }
    }
}
