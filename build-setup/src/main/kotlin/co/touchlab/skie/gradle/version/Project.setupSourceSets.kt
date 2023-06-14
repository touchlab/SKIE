package co.touchlab.skie.gradle.version

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import kotlin.io.path.name

fun <CELL: Comparable<CELL>> Project.setupSourceSets(
    matrix: DependencyMatrix<CELL>,
    configureTarget: KotlinJvmTarget.(CELL) -> Unit,
    configureSourceSet: SourceSetScope<CELL, SourceSetScope.Target<CELL>>.() -> Unit,
) = with(matrix) {
    val sourceDir = project.file("src").toPath()
    val testsDir = project.file("tests").toPath()

    val versionRanges = matrix.sourceSetRangesIn(sourceDir, testsDir)
    val rootTarget = SourceSetScope.Target.Root(matrix.oldest)
    val commonTargets = matrix.commonTargets()
    val leafTargets = matrix.cells.map {
        SourceSetScope.Target.Leaf(it)
    }
    val rangeTargets = versionRanges.map {
        SourceSetScope.Target.Range(it)
    }

    extensions.configure<KotlinMultiplatformExtension> {
        targets {
            matrix.cells.forEach { version ->
                jvm(matrix.identifierFor(version)) {
                    configureTarget(version)
                }
            }
        }

        val allSourceSets = SourceSetScope.Compilation.values().flatMap { compilation ->
            val root = prepare(rootTarget, compilation, matrix)
            val common = commonTargets.map { prepare(it, compilation, matrix) }
            val ranges = rangeTargets.map { prepare(it, compilation, matrix) }
            val leaves = leafTargets.map { prepare(it, compilation, matrix) }

            val all = listOf(listOf(root), common, ranges, leaves).flatten()
            all.forEach { sourceSet ->
                sourceSet.apply {
                    val root = when (compilation) {
                        SourceSetScope.Compilation.Main -> sourceDir.name
                        SourceSetScope.Compilation.Test -> testsDir.name
                    }
                    val path = sourceSet.target.pathComponents().joinToString("/")
                    kotlinSourceSet.kotlin.setSrcDirs(
                        listOf("$root/$path/kotlin")
                    )
                    kotlinSourceSet.resources.setSrcDirs(
                        listOf("$root/$path/resources")
                    )
                }
            }

            matrix.configureSourceSetHierarchy(root, common, ranges, leaves)

            all
        }

//         val commonMains = commonTargets.map { prepare(it, SourceSetScope.Compilation.Main, matrix) }
//         val commonTests = commonTargets.map { prepare(it, SourceSetScope.Compilation.Test, matrix) }
//         val rangeTests = rangeTargets.map { prepare(it, SourceSetScope.Compilation.Test, matrix) }
//         val leafMains = leafTargets.map { prepare(it, SourceSetScope.Compilation.Main, matrix) }
//         val leafTests = leafTargets.map { prepare(it, SourceSetScope.Compilation.Test, matrix) }
//
//         val allSourceSets = listOf(
//             commonMains,
//             commonTests,
//             rangeMains,
//             rangeTests,
//             leafMains,
//             leafTests,
//         ).flatten()
//
//         allSourceSets
//
//         matrix.configureSourceSetHierarchy(commonMains, rangeMains, leafTargets)
//
//         rangeMains.configureRangeSourceSetDependencies()
//         rangeTests.configureRangeSourceSetDependencies()
//
//         leafMains.configureLeafSourceSetDependencies(rangeMains)
//         leafTests.configureLeafSourceSetDependencies(rangeTests)

        allSourceSets.forEach(configureSourceSet)
    }
}

private fun <CELL: Comparable<CELL>, TARGET: SourceSetScope.Target<CELL>> KotlinMultiplatformExtension.prepare(
    target: TARGET,
    compilation: SourceSetScope.Compilation,
    matrix: DependencyMatrix<CELL>,
): SourceSetScope<CELL, TARGET> = with(matrix) {
    return SourceSetScope(
        sourceSets.maybeCreate(target.sourceSetName(compilation)),
        compilation,
        target,
    )
}

fun <T> ClosedRange<T>.contains(other: ClosedRange<T>): Boolean where T: Comparable<T> {
    return start <= other.start && endInclusive >= other.endInclusive
}
