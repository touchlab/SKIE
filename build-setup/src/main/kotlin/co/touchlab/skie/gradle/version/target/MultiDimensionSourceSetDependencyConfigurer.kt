package co.touchlab.skie.gradle.version.target

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class MultiDimensionSourceSetDependencyConfigurer {
    fun configure(sourceSets: Map<SourceSet, KotlinSourceSet>) {
        val sourceSetPairs = sourceSets.map { (sourceSet, kotlinSourceSet) ->
            SourceSetPair(
                sourceSet = sourceSet,
                kotlinSourceSet = kotlinSourceSet,
            )
        }

        val allDependencies = sourceSetPairs.associateWith { (sourceSet, _) ->
            if (!sourceSet.isRoot) {
                sourceSetPairs.filter { (otherSourceSet, _) ->
                    sourceSet.shouldDependOn(otherSourceSet)
                }.toSet()
            } else {
                emptySet()
            }
        }

        val directDependencies = allDependencies.mapValues { it.value.toMutableSet() }
        fun allDependencies(sourceSet: SourceSetPair): Set<SourceSetPair> {
            return directDependencies.getOrDefault(sourceSet, emptySet()).flatMap { dependency ->
                setOf(dependency) + allDependencies(dependency)
            }.toSet()
        }

        do {
            var hasDoneWork = false

            directDependencies.forEach { (sourceSetPair, dependencies) ->
                val transitiveDependencies = dependencies.flatMap { allDependencies(it) }.toSet()
                val dependenciesToRemove = dependencies intersect transitiveDependencies
                if (dependenciesToRemove.isNotEmpty()) {
                    dependencies.removeAll(dependenciesToRemove)
                    hasDoneWork = true
                }
            }
        } while (hasDoneWork)

        directDependencies.forEach { (sourceSetPair, dependencies) ->
            val kotlinSourceSet = sourceSetPair.kotlinSourceSet
            dependencies.forEach { dependency ->
                kotlinSourceSet.dependsOn(dependency.kotlinSourceSet)
            }
        }
    }

    private data class SourceSetPair(
        val sourceSet: SourceSet,
        val kotlinSourceSet: KotlinSourceSet,
    )
}
