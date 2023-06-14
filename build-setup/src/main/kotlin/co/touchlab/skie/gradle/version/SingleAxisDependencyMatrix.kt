package co.touchlab.skie.gradle.version

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

class SingleAxisDependencyMatrix<VERSION: Comparable<VERSION>>(
    val axis: DependencyMatrix.Axis<VERSION>,
): DependencyMatrix<VERSION> {
    override val cells: List<VERSION> = axis.labels.sorted()
    override val oldest: VERSION = cells.first()

    override fun commonTargets(): List<SourceSetScope.Target.Common<VERSION>> = emptyList()

    override fun sourceSetRangesIn(vararg directories: Path): List<ClosedRange<VERSION>> {
        return directories.flatMap { directory ->
            if (directory.notExists()) return@flatMap emptyList()
            directory.listDirectoryEntries(DependencyMatrix.versionRangeGlob).map { file ->
                axis.resolveRange(file.name)
            }
        }.toSet().sortedBy { it.start }
    }

    override fun identifierFor(cell: VERSION): String {
        return axis.targetNameFor(cell)
    }

    override fun SourceSetScope.Target<VERSION>.sourceSetName(compilation: SourceSetScope.Compilation): String {
        return when (this) {
            is SourceSetScope.Target.Root, is SourceSetScope.Target.Common -> when (compilation) {
                SourceSetScope.Compilation.Main -> KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME
                SourceSetScope.Compilation.Test -> KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME
            }
            is SourceSetScope.Target.Range, is SourceSetScope.Target.Leaf -> pathComponents()
                .joinToString(DependencyMatrix.axisTargetDelimiter, postfix = compilation.name)
        }
    }

    override fun SourceSetScope.Target<VERSION>.pathComponents(): List<String> {
        return when (this) {
            is SourceSetScope.Target.Root, is SourceSetScope.Target.Common -> listOf("common")
            is SourceSetScope.Target.Range -> range.pathComponents()
            is SourceSetScope.Target.Leaf -> dependencies.pathComponents()
        }
    }

    override fun configureSourceSetHierarchy(
        root: SourceSetScope<VERSION, SourceSetScope.Target.Root<VERSION>>,
        common: List<SourceSetScope<VERSION, SourceSetScope.Target.Common<VERSION>>>,
        ranges: List<SourceSetScope<VERSION, SourceSetScope.Target.Range<VERSION>>>,
        leaves: List<SourceSetScope<VERSION, SourceSetScope.Target.Leaf<VERSION>>>
    ) {
        require(common.isEmpty()) { "SingleAxisDependencyMatrix does not support common targets" }

        ranges.forEach { sourceSet ->
            sourceSet.dependsOn(root)

            ranges.forEach {
                if (it.target.range.contains(sourceSet.target.range) && sourceSet != it) {
                    sourceSet.dependsOn(it)
                }
            }
        }

        leaves.forEach { sourceSet ->
            sourceSet.dependsOn(root)

            ranges.forEach {
                if (it.target.range.contains(sourceSet.target.dependencies)) {
                    sourceSet.dependsOn(it)
                }
            }
        }
    }

    private fun VERSION.pathComponents(): List<String> = listOf(axis.targetNameFor(this))

    private fun ClosedRange<VERSION>.pathComponents(): List<String> = listOf(
        listOf(
            axis.targetNameFor(start),
            axis.targetNameFor(endInclusive),
        ).joinToString(DependencyMatrix.rangeDelimiter)
    )
}
