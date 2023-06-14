package co.touchlab.skie.gradle.version

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

class KotlinPluginShimDependencyMatrix(
    private val kotlinToolingVersionAxis: KotlinToolingVersionAxis,
    private val gradleApiVersionAxis: GradleApiVersionAxis,
): DependencyMatrix<KotlinPluginShimDependencyMatrix.Cell> {
    override val cells: List<Cell> = kotlinToolingVersionAxis.labels.flatMap { kotlinToolingVersion ->
        gradleApiVersionAxis.labels.map { gradleApiVersion ->
            Cell(kotlinToolingVersion, gradleApiVersion)
        }
    }.sorted()

    override val oldest: Cell = cells.first()

    override fun commonTargets(): List<SourceSetScope.Target.Common<Cell>> {
        val oldestKotlin = kotlinToolingVersionAxis.labels.min()
        val oldestGradle = gradleApiVersionAxis.labels.min()

        return listOf(
            kotlinToolingVersionAxis.labels.map { kotlin ->
                SourceSetScope.Target.Common(
                    Cell(kotlin, oldestGradle),
                    gradleApiVersionAxis,
                )
            },
            gradleApiVersionAxis.labels.map { gradle ->
                SourceSetScope.Target.Common(
                    Cell(oldestKotlin, gradle),
                    kotlinToolingVersionAxis,
                )
            }
        ).flatten()
    }

    override fun sourceSetRangesIn(vararg directories: Path): List<ClosedRange<Cell>> {
        return directories.flatMap { directory ->
            if (directory.notExists()) return@flatMap emptyList()
            directory.listDirectoryEntries(DependencyMatrix.versionRangeGlob).flatMap { kotlinSourceSetDir ->
                val kotlinRange = kotlinToolingVersionAxis.resolveRange(kotlinSourceSetDir.name.removePrefix(kotlinToolingVersionPrefix))
                kotlinSourceSetDir.listDirectoryEntries(DependencyMatrix.versionRangeGlob).map { gradleSourceSetDir ->
                    val gradleRange = gradleApiVersionAxis.resolveRange(gradleSourceSetDir.name.removePrefix(gradleApiVersionPrefix))
                    Cell(kotlinRange.start, gradleRange.start)..Cell(kotlinRange.endInclusive, gradleRange.endInclusive)
                }
            }
        }.toSet().sortedBy { it.start }
    }

    private fun <TARGET: SourceSetScope.Target<Cell>> KotlinMultiplatformExtension.prepare(
        target: TARGET,
        compilation: SourceSetScope.Compilation,
    ): SourceSetScope<Cell, TARGET> {
        return SourceSetScope(
            sourceSets.maybeCreate(target.sourceSetName(compilation)),
            compilation,
            target,
        )
    }

    override fun identifierFor(cell: Cell): String {
        return cell.pathComponents().joinToString(DependencyMatrix.axisTargetDelimiter)
    }

    override fun SourceSetScope.Target<Cell>.sourceSetName(compilation: SourceSetScope.Compilation): String {
        return if (this is SourceSetScope.Target.Root) {
            when (compilation) {
                SourceSetScope.Compilation.Main -> KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME
                SourceSetScope.Compilation.Test -> KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME
            }
        } else {
            pathComponents().joinToString(DependencyMatrix.axisTargetDelimiter, postfix = compilation.name)
        }
    }

    override fun SourceSetScope.Target<Cell>.pathComponents(): List<String> {
        return when (this) {
            is SourceSetScope.Target.Root -> pathComponents("common", "common")
            is SourceSetScope.Target.Common -> {
                val common = oldest
                val kotlinName = if (commonAxis == kotlinToolingVersionAxis) {
                    "common"
                } else {
                    dependencies.kotlinToolingVersion.toString()
                }
                val gradleName = if (commonAxis == gradleApiVersionAxis) {
                    "common"
                } else {
                    dependencies.gradleApiVersion.gradleVersion.version
                }
                pathComponents(kotlinName, gradleName)
            }
            is SourceSetScope.Target.Range -> range.pathComponents()
            is SourceSetScope.Target.Leaf -> dependencies.pathComponents()
        }
    }

    override fun configureSourceSetHierarchy(
        root: SourceSetScope<Cell, SourceSetScope.Target.Root<Cell>>,
        common: List<SourceSetScope<Cell, SourceSetScope.Target.Common<Cell>>>,
        ranges: List<SourceSetScope<Cell, SourceSetScope.Target.Range<Cell>>>,
        leaves: List<SourceSetScope<Cell, SourceSetScope.Target.Leaf<Cell>>>
    ) {
        common.forEach { sourceSet ->
            sourceSet.dependsOn(root)
        }

        ranges.forEach { sourceSet ->
            sourceSet.dependsOn(root)

            common.forEach {
                val isKotlinCompatible = it.target.commonAxis == kotlinToolingVersionAxis
                    || sourceSet.target.range.contains(it.target.dependencies.kotlinToolingVersion)
                val isGradleCompatible = it.target.commonAxis == gradleApiVersionAxis
                    || sourceSet.target.range.contains(it.target.dependencies.gradleApiVersion)

                if (isKotlinCompatible && isGradleCompatible) {
                    sourceSet.dependsOn(it)
                }
            }

            ranges.forEach {
                if (it.target.range.contains(sourceSet.target.range) && sourceSet != it) {
                    sourceSet.dependsOn(it)
                }
            }
        }

        leaves.forEach { sourceSet ->
            sourceSet.dependsOn(root)

            common.forEach {
                val isKotlinCompatible = it.target.commonAxis == kotlinToolingVersionAxis
                    || it.target.dependencies.kotlinToolingVersion == sourceSet.target.dependencies.kotlinToolingVersion
                val isGradleCompatible = it.target.commonAxis == gradleApiVersionAxis
                    || it.target.dependencies.gradleApiVersion == sourceSet.target.dependencies.gradleApiVersion

                if (isKotlinCompatible && isGradleCompatible) {
                    sourceSet.dependsOn(it)
                }
            }

            ranges.forEach {
                if (it.target.range.contains(sourceSet.target.dependencies)) {
                    sourceSet.dependsOn(it)
                }
            }
        }
    }

    private fun Cell.pathComponents(): List<String> = pathComponents(
        kotlinToolingVersion.toString(),
        gradleApiVersion.gradleVersion.version,
    )

    private fun ClosedRange<Cell>.pathComponents(): List<String> = pathComponents(
        listOf(
            start.kotlinToolingVersion,
            endInclusive.kotlinToolingVersion,
        ).joinToString(DependencyMatrix.rangeDelimiter),
        listOf(
            gradleApiVersionAxis.targetNameFor(start.gradleApiVersion),
            start.gradleApiVersion.gradleVersion.version,
            endInclusive.gradleApiVersion.gradleVersion.version,
        ).joinToString(DependencyMatrix.rangeDelimiter),
    )

    private fun pathComponents(kotlin: String, gradle: String): List<String> = listOf(
        "${kotlinToolingVersionPrefix}$kotlin",
        "${gradleApiVersionPrefix}$gradle",
    )

    private fun ClosedRange<Cell>.contains(kotlin: KotlinToolingVersion): Boolean {
        return start.kotlinToolingVersion <= kotlin && kotlin <= endInclusive.kotlinToolingVersion
    }

    private fun ClosedRange<Cell>.contains(gradle: GradleApiVersion): Boolean {
        return start.gradleApiVersion <= gradle && gradle <= endInclusive.gradleApiVersion
    }

    data class Cell(
        val kotlinToolingVersion: KotlinToolingVersion,
        val gradleApiVersion: GradleApiVersion,
    ): Comparable<Cell> {
        override fun compareTo(other: Cell): Int {
            return compareValuesBy(this, other, Cell::kotlinToolingVersion, Cell::gradleApiVersion)
        }
    }

    companion object {
        val kotlinToolingVersionPrefix = "kgp_"
        val gradleApiVersionPrefix = "gradle_"
    }
}
