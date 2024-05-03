package co.touchlab.skie.gradle.version.target

import co.touchlab.skie.gradle.version.target.SourceSet.ComponentSet
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

class MultiDimensionSourceSetConfigurer(
    private val project: Project,
    private val dimensions: List<Target.Dimension<*>>,
    private val targetConfigurer: MultiDimensionTargetConfigurer,
    private val sourceSetConfigureActions: List<ConfigureSourceSetScope.(SourceSet) -> Unit>,
) {

    private val dependencyConfigurer = MultiDimensionSourceSetDependencyConfigurer()

    fun configure() {
        val compilations = listOf(
            MultiDimensionTargetPlugin.Compilation.Main(project.file("src").toPath()),
            MultiDimensionTargetPlugin.Compilation.Test(project.file("tests").toPath()),
        )
        val allTargets = targetConfigurer.allTargets.toList()

        val kotlinExtension = project.extensions.getByType<KotlinMultiplatformExtension>()
        kotlinExtension.targets.forEach { it.compilations.getByName("main").implementationConfigurationName }
        compilations.forEach { compilation ->
            val allSourceSets = resolveSourceSets(compilation.directory, dimensions, allTargets)
                .associateWith { sourceSet ->
                    if (sourceSet.isIntermediate) {
                        kotlinExtension.sourceSets.maybeCreate(sourceSet.name + "__" + compilation.sourceSetNameSuffix)
                    } else {
                        kotlinExtension.sourceSets.getByName(sourceSet.name + compilation.sourceSetNameSuffix)
                    }
                }

            dependencyConfigurer.configure(allSourceSets)

            allSourceSets.forEach { (sourceSet, kotlinSourceSet) ->
                kotlinSourceSet.apply {
                    kotlin.srcDirs.remove(project.projectDir.resolve("src/${sourceSet.name}/kotlin"))
                    kotlin.srcDirs(
                        compilation.kotlinSourcePaths(sourceSet),
                    )

                    resources.srcDirs.remove(project.projectDir.resolve("src/${sourceSet.name}/resources"))
                    resources.srcDirs(
                        compilation.resourcePaths(sourceSet),
                    )
                }

                val configureScope = DefaultConfigureSourceSetScope(project, kotlinSourceSet, compilation)
                sourceSetConfigureActions.forEach { it(configureScope, sourceSet) }
            }
        }
    }

    private fun resolveSourceSets(
        directory: Path,
        dimensions: List<Target.Dimension<*>>,
        targets: List<Target>,
    ): Set<SourceSet> {
        val targetSourceSets = targets.map { target ->
            val components = target.components.zip(dimensions).map { (component, dimension) ->
                ComponentSet.Specific.unsafe(
                    name = dimension.prefix + component.value,
                    dimension = dimension,
                    component = component,
                )
            }

            SourceSet(
                name = target.name,
                components = components,
                sourceDirs = listOf(
                    RelativePath(components.map { it.name }),
                ),
                target = target,
            )
        }

        val rootSourceSet = SourceSet(
            name = "common",
            components = dimensions.map { dimension ->
                ComponentSet.Common(
                    name = dimension.prefix + dimension.commonName,
                    dimension = dimension,
                )
            },
            sourceDirs = listOf(
                RelativePath(
                    listOf("common"),
                ),
            ),
            isRoot = true,
        )

        val sourceSetDirectories = resolveSourceSetDirectories(directory, dimensions.first(), dimensions.drop(1))
        val usedIntermediateSourceSets = sourceSetDirectories.toIntermediateSourceSets(dimensions)
        val allCommonSourceSets = setOf(rootSourceSet) + usedIntermediateSourceSets

        val flattenedCommonSourceSets = allCommonSourceSets
            .groupBy { sourceSet ->
                sourceSet.components.map { it.withErasedIdentity() }
            }
            .map { (components, sourceSets) ->
                if (sourceSets.size == 1) {
                    sourceSets.single()
                } else {
                    SourceSet(
                        name = sourceSets.firstOrNull { it.isRoot }?.name ?: components.joinToString("__") { it.name },
                        components = components,
                        sourceDirs = sourceSets.flatMap { it.sourceDirs },
                        isRoot = sourceSets.any { it.isRoot },
                    )
                }
            }
            .toSet()

        return flattenedCommonSourceSets + targetSourceSets
    }

    private fun resolveSourceSetDirectories(
        parentDirectory: Path,
        dimension: Target.Dimension<*>,
        nextDimensions: List<Target.Dimension<*>>,
    ): List<SourceSet.Directory> {
        if (parentDirectory.notExists()) {
            return emptyList()
        }

        return parentDirectory.listDirectoryEntries().mapNotNull { sourceSetDir ->
            val components = dimension.parse(sourceSetDir.name)
            if (components == null || components.isEmpty()) {
                return@mapNotNull null
            }

            SourceSet.Directory(
                name = sourceSetDir.name,
                components = components,
                children = nextDimensions.firstOrNull()?.let { nextDimension ->
                    resolveSourceSetDirectories(sourceSetDir, nextDimension, nextDimensions.drop(1))
                } ?: emptyList(),
            )
        }
    }

    private fun List<SourceSet.Directory>.toIntermediateSourceSets(dimensions: List<Target.Dimension<*>>): Set<SourceSet> =
        this.flatMap { directory ->
            if (directory.children.isEmpty()) {
                if (dimensions.size == 1) {
                    setOf(
                        SourceSet(
                            name = directory.name,
                            components = listOf(directory.components),
                            sourceDirs = listOf(RelativePath(listOf(directory.name))),
                        ),
                    )
                } else {
                    emptySet()
                }
            } else {
                directory.children.toIntermediateSourceSets(dimensions.drop(1)).map { childSourceSet ->
                    SourceSet(
                        name = directory.name + "__" + childSourceSet.name,
                        components = listOf(directory.components) + childSourceSet.components,
                        sourceDirs = childSourceSet.sourceDirs.map {
                            it.copy(components = listOf(directory.name) + it.components)
                        },
                    )
                }
            }
        }
            .filterNot { it.components.all { componentSet -> componentSet is ComponentSet.Specific } }
            .toSet()
}
