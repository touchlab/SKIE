package co.touchlab.skie.gradle.version.target

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinVariant
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

abstract class MultiDimensionTargetPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<MultiDimensionTargetExtension>("multiDimensionTarget", {
            doWork(project, project.extensions.getByType())
        })

        project.afterEvaluate {
            check(extension.dimensions.isPresent)
            check(extension.createTarget.isPresent)
            check(extension.configureSourceSet.isPresent)

            /** TODO: We want to run in afterEvaluate instead of using the hack with `onConfigurationComplete`
             *      but we can't because targets created in `afterEvaluate` don't respect our custom attributes.
             */
//             doWork(project, project.extensions.getByType())
        }
    }

    private fun doWork(project: Project, extension: MultiDimensionTargetExtension) {
        val dimensions = extension.dimensions.get()
        val allTargets = dimensions
            .fold(tupleSpaceOf<Target.ComponentInDimension<*>>(tupleOf())) { acc, dimension ->
                acc * dimension.componentsWithDimension
            }
            .map { tuple ->
                Target(
                    name = tuple.joinToString("__") { it.componentName },
                    components = tuple.map { it.component }
                )
            }

        val compilations = listOf(
            Compilation.Main(project.file("src").toPath()),
            Compilation.Test(project.file("tests").toPath()),
        )

        project.extensions.configure<KotlinMultiplatformExtension> {
            allTargets.forEach { target ->
                val kotlinTarget = extension.createTarget.get().invoke(this, target)
                // applyUserDefinedAttributes(kotlinTarget)
            }

            compilations.forEach { compilation ->
                val allSourceSets = resolveSourceSets(compilation.directory, dimensions, allTargets)
                    .associateWith { sourceSet ->
                        if (sourceSet.isIntermediate) {
                            sourceSets.maybeCreate(sourceSet.name + compilation.sourceSetNameSuffix)
                        } else {
                            sourceSets.getByName(sourceSet.name + compilation.sourceSetNameSuffix)
                        }
                    }

                allSourceSets.forEach { (sourceSet, kotlinSourceSet) ->
                    if (sourceSet.isRoot) { return@forEach }

                    val dependencies = allSourceSets.filter { (otherSourceSet, _) ->
                        sourceSet.shouldDependOn(otherSourceSet)
                    }

                    dependencies.forEach { (_, otherKotlinSourceSet) ->
                        println("Adding dependency from ${kotlinSourceSet.name} to ${otherKotlinSourceSet.name}")
                        kotlinSourceSet.dependsOn(otherKotlinSourceSet)
                    }
                }

                allSourceSets.forEach { (sourceSet, kotlinSourceSet) ->
                    kotlinSourceSet.apply {
                        println("Configuring source set ${kotlinSourceSet.name} - \n${compilation.kotlinSourcePaths(sourceSet).joinToString("\n") { "\t- $it" }}")
                        kotlin.setSrcDirs(
                            compilation.kotlinSourcePaths(sourceSet),
                        )

                        resources.setSrcDirs(
                            compilation.resourcePaths(sourceSet),
                        )
                    }

                    val configureScope = if (sourceSet.isTarget) {
                        StrictConfigureSourceSetScope(project, kotlinSourceSet, compilation)
                    } else {
                        FloorRequirementConfigureSourceSetScope(project, kotlinSourceSet, compilation)
                    }

                    extension.configureSourceSet.get().invoke(configureScope, sourceSet)
                }
            }
        }
    }

    private fun resolveSourceSets(
        directory: Path,
        dimensions: List<Target.Dimension<*>>,
        targets: List<Target>,
    ): Set<SourceSet> {
        fun sourceSetName(components: List<SourceSet.ComponentSet<out Target.Component>>): String {
            return components.map { component ->
                component.dimension.prefix + component.name
            }.joinToString("__")
        }

        fun List<SourceSet.Directory>.sourceSets(): Set<SourceSet> {
            return flatMap { directory ->
                if (directory.children.isEmpty()) {
                    setOf(
                        SourceSet(
                            name = directory.name,
                            components = listOf(directory.components),
                            sourceDirs = listOf(RelativePath(listOf(directory.name))),
                        )
                    )
                } else {
                    directory.children.sourceSets().map { childSourceSet ->
                        SourceSet(
                            name = directory.name + "__" + childSourceSet.name,
                            components = listOf(directory.components) + childSourceSet.components,
                            sourceDirs = childSourceSet.sourceDirs.map {
                                it.copy(components = listOf(directory.name) + it.components)
                            }
                        )
                    }
                }
            }.toSet()
        }

        val sourceSetDirectories = resolveSourceSetDirectories(directory, dimensions.first(), dimensions.drop(1))

        val targetSourceSets = targets.map { target ->
            val components = target.components.zip(dimensions).map { (component, dimension) ->
                SourceSet.ComponentSet.Specific.unsafe(
                    name = dimension.prefix + component.value,
                    dimension = dimension,
                    component = component,
                )
            }

            SourceSet(
                name = target.name,
                components = components,
                sourceDirs = listOf(
                    RelativePath(components.map { it.name })
                )
            )
        }

        val sourceSetsFromDirectories = sourceSetDirectories.sourceSets()
        val intermediateSourceSets = sourceSetsFromDirectories.filter { it.isIntermediate }

        val flattenedIntermediateSourceSets = intermediateSourceSets
            .groupBy { sourceSet ->
                sourceSet.components.map { it.withErasedIdentity() }
            }
            .map { (components, sourceSets) ->
                if (sourceSets.size == 1) {
                    sourceSets.single()
                } else {
                    SourceSet(
                        name = sourceSetName(components),
                        components = components,
                        sourceDirs = sourceSets.flatMap { it.sourceDirs },
                    )
                }
            }
            .toSet()

        val rootSourceSet = SourceSet(
            name = "common",
            components = dimensions.map { dimension ->
                SourceSet.ComponentSet.Common(
                    name = dimension.prefix + dimension.commonName,
                    dimension = dimension,
                )
            },
            sourceDirs = listOf(
                RelativePath(
                    listOf("common")
                ),
            ),
            isRoot = true,
        )

        return setOf(rootSourceSet) + flattenedIntermediateSourceSets + targetSourceSets
    }

    private fun resolveSourceSetDirectories(
        parentDirectory: Path,
        dimension: Target.Dimension<*>,
        nextDimensions: List<Target.Dimension<*>>,
    ): List<SourceSet.Directory> {
        if (parentDirectory.notExists()) { return emptyList() }

        return parentDirectory.listDirectoryEntries().mapNotNull { sourceSetDir ->
            val components = dimension.parse(sourceSetDir.name)
            if (components == null || components.isEmpty()) { return@mapNotNull null }

            SourceSet.Directory(
                name = sourceSetDir.name,
                components = components,
                children = nextDimensions.firstOrNull()?.let { nextDimension ->
                    resolveSourceSetDirectories(sourceSetDir, nextDimension, nextDimensions.drop(1))
                } ?: emptyList(),
            )
        }
    }

    sealed interface Compilation {
        val sourceSetNameSuffix: String
        val directory: Path

        data class Main(override val directory: Path): Compilation {
            override val sourceSetNameSuffix: String = "Main"
        }

        data class Test(override val directory: Path): Compilation {
            override val sourceSetNameSuffix: String = "Test"
        }

        fun kotlinSourcePaths(sourceSet: SourceSet): List<String> = sourcePaths(sourceSet, "kotlin")

        fun resourcePaths(sourceSet: SourceSet): List<String> = sourcePaths(sourceSet, "resources")

        private fun sourcePaths(sourceSet: SourceSet, sourceName: String): List<String> {
            return sourceSet.sourceDirs.map { path ->
                val allPathComponents = listOf(
                    directory.name,
                ) + path.components + listOf(
                    sourceName
                )
                allPathComponents.joinToString(File.separator)
            }
        }
    }
}
