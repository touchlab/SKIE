package co.touchlab.skie.gradle.version

import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class SourceSetScope<CELL: Comparable<CELL>, out TARGET: SourceSetScope.Target<CELL>>(
    val kotlinSourceSet: KotlinSourceSet,
    val compilation: Compilation,
    val target: TARGET,
) {
    fun addWeakDependency(dependency: String, configure: ExternalModuleDependency.() -> Unit = {}) {
        kotlinSourceSet.dependencies {
            when (compilation) {
                Compilation.Main -> compileOnly(dependency, configure)
                Compilation.Test -> implementation(dependency, configure)
            }
        }
    }

    fun addPlatform(notation: Any) {
        kotlinSourceSet.dependencies {
            val dependency = when (val target = target as Target<CELL>) {
                is Target.Root, is Target.Common, is Target.Range -> platform(notation)
                is Target.Leaf -> enforcedPlatform(notation)
            }
            when (compilation) {
                Compilation.Main -> compileOnly(dependency)
                Compilation.Test -> implementation(dependency)
            }
        }
    }

    fun configureVersion(version: String): ExternalModuleDependency.() -> Unit = {
        version {
            when (val target = target as Target<CELL>) {
                is Target.Root, is Target.Common, is Target.Range -> require(version)
                is Target.Leaf -> strictly(version)
            }
        }
    }

    fun dependsOn(other: SourceSetScope<CELL, *>) {
        kotlinSourceSet.dependsOn(other.kotlinSourceSet)
    }

    sealed interface Target<CELL: Comparable<CELL>> {
        val baseValue: CELL
            get() = when (this) {
                is Common -> dependencies
                is Leaf -> dependencies
                is Range -> range.start
                is Root -> dependencies
            }

        data class Root<CELL: Comparable<CELL>>(
            val dependencies: CELL,
        ): Target<CELL>

        data class Common<CELL: Comparable<CELL>>(
            val dependencies: CELL,
            // TODO: This is a hack for KotlinPluginShimDependencyMatrix to know which axis is common
            val commonAxis: DependencyMatrix.Axis<*>,
        ): Target<CELL>

        data class Range<CELL: Comparable<CELL>>(
            val range: ClosedRange<CELL>,
        ): Target<CELL>

        data class Leaf<CELL: Comparable<CELL>>(
            val dependencies: CELL,
        ): Target<CELL>
    }

    enum class Compilation {
        Main, Test
    }
}
