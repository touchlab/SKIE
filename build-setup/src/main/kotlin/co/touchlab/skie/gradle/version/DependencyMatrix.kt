package co.touchlab.skie.gradle.version

import java.nio.file.Path

interface DependencyMatrix<CELL: Comparable<CELL>> {
    val cells: List<CELL>

    val oldest: CELL

    fun identifierFor(cell: CELL): String

    fun SourceSetScope.Target<CELL>.sourceSetName(compilation: SourceSetScope.Compilation): String

    fun SourceSetScope.Target<CELL>.pathComponents():List<String>

    fun commonTargets(): List<SourceSetScope.Target.Common<CELL>>

    fun sourceSetRangesIn(vararg directories: Path): List<ClosedRange<CELL>>

    fun configureSourceSetHierarchy(
        root: SourceSetScope<CELL, SourceSetScope.Target.Root<CELL>>,
        common: List<SourceSetScope<CELL, SourceSetScope.Target.Common<CELL>>>,
        ranges: List<SourceSetScope<CELL, SourceSetScope.Target.Range<CELL>>>,
        leaves: List<SourceSetScope<CELL, SourceSetScope.Target.Leaf<CELL>>>,
    )

    fun findCell(identifier: String): CELL? {
        return cells.find { identifierFor(it) == identifier }
    }

    interface Axis<LABEL: Comparable<LABEL>> {
        val name: String
        val labels: List<LABEL>

        fun targetNameFor(label: LABEL): String

        fun findLabel(identifier: String): LABEL? {
            return labels.find { targetNameFor(it) == identifier }
        }

        fun resolveRange(value: String): ClosedRange<LABEL> {
            val versionRange = value.split(rangeDelimiter)
            check(versionRange.size == 2) { "Invalid version range: $value" }

            val (start, end) = versionRange
            val startVersion = findLabel(start) ?: error("Invalid start version: $start")
            val endVersion = findLabel(end) ?: error("Invalid end version: $end")
            check(startVersion < endVersion) { "Invalid version range: $startVersion >= $endVersion" }
            return startVersion..endVersion
        }
    }

    companion object {
        const val rangeDelimiter = ".."
        const val axisTargetDelimiter = "__"

        const val versionRangeGlob = "*$rangeDelimiter*"

        fun isPossibleVersionRange(value: String) = value.split(rangeDelimiter).size == 2
    }
}
