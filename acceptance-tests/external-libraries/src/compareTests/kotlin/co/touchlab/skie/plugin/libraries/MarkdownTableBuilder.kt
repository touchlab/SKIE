package co.touchlab.skie.plugin.libraries

import kotlin.math.max
import kotlin.time.DurationUnit

class MarkdownTableBuilder private constructor(
    private val header: List<String>,
) {

    private val rows = mutableListOf<List<String>>()

    fun addRow(vararg cells: String): MarkdownTableBuilder {
        rows.add(cells.take(header.size))
        return this
    }

    fun <T: Comparable<T>> addSummaryRow(name: String, summary: ComparisonReport.Stats.Item<T>, toString: (T) -> String): MarkdownTableBuilder {
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
        fun statisticsTableHeader(report: ComparisonReport) = MarkdownTableBuilder(
            listOf(
                "Statistic",
                "Min",
                "Max",
                "Total",
                "Average",
                "Median",
                "Standard Deviation",
            )
        ).apply {
            addSummaryRow("SKIE disabled", report.stats.pure) { it.toSecondsString() }
            addSummaryRow("SKIE enabled", report.stats.skie) { it.toSecondsString() }
            addSummaryRow("Absolute difference", report.stats.absoluteDifference) { it.toSecondsString() }
            addSummaryRow("Relative difference", report.stats.relativeDifference) { it.toPercentString() }
        }
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
