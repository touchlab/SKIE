package co.touchlab.skie.gradle.version.target

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

class MultiDimensionTargetConfigurer(
    private val project: Project,
) {
    val allTargets = project.objects.namedDomainObjectList(Target::class)

    fun configure(
        dimensions: List<Target.Dimension<*>>,
        createTarget: KotlinMultiplatformExtension.(Target) -> KotlinTarget,
    ) {
        dimensions
            .fold(tupleSpaceOf<Target.ComponentInDimension<*>>(tupleOf())) { acc, dimension ->
                acc * dimension.componentsWithDimension
            }
            .forEach { tuple ->
                allTargets.add(
                    Target(
                        tuple.joinToString("__") { it.componentName },
                        tuple.map { it.component },
                    ),
                )
            }

        val kotlin = project.extensions.getByType<KotlinMultiplatformExtension>()
        allTargets.forEach { target ->
            val kotlinTarget = kotlin.createTarget(target)
//             kotlinTarget.applyUserDefinedAttributes()
        }
    }
}

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
                    println("Configuring source set ${kotlinSourceSet.name} - \n${compilation.kotlinSourcePaths(sourceSet).joinToString("\n") { "\t- $it" }}")
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

            println("\n\nAll source sets for compilation ${compilation.sourceSetNameSuffix}:")
            allSourceSets.forEach { (sourceSet, kotlinSourceSet) ->
                println("\tSS: ${sourceSet.name}, KSS: ${kotlinSourceSet.name}")
                println(sourceSet.components.joinToString(prefix = "\t\t[", postfix = "]") {
                    it.components.joinToString(prefix = "{", postfix = "}") { it.value }
                })
                println(sourceSet.sourceDirs.joinToString("\n") {
                    "\t\t- " + it.components.joinToString("/")
                })
                println(kotlinSourceSet.dependsOn.joinToString(prefix = "\t\tDepends on = [", postfix = "]") { it.name })
            }
            println("\n\n")
        }
    }

    private fun resolveSourceSets(
        directory: Path,
        dimensions: List<Target.Dimension<*>>,
        targets: List<Target>,
    ): Set<SourceSet> {
        fun List<SourceSet.Directory>.sourceSets(dimensions: List<Target.Dimension<*>>): Set<SourceSet> {
            return flatMap { directory ->
                if (directory.children.isEmpty()) {
                    if (dimensions.size == 1) {
                        setOf(
                            SourceSet(
                                name = directory.name,
                                components = listOf(directory.components),
                                sourceDirs = listOf(RelativePath(listOf(directory.name))),
                            )
                        )
                    } else {
                        emptySet()
                    }
                } else {
                    directory.children.sourceSets(dimensions.drop(1)).map { childSourceSet ->
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

        val sourceSetsFromDirectories = sourceSetDirectories.sourceSets(dimensions)
        val intermediateSourceSets = sourceSetsFromDirectories.filter { it.isIntermediate }

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

        val allCommonSourceSets = setOf(rootSourceSet) + intermediateSourceSets

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
}

abstract class MultiDimensionTargetPlugin(): Plugin<Project> {


    override fun apply(project: Project) {
        project.apply<KotlinMultiplatformPluginWrapper>()

        val targetConfigurer = MultiDimensionTargetConfigurer(project)
        val extension = project.extensions.create<MultiDimensionTargetExtension>(
            "multiDimensionTarget",
            targetConfigurer,
        )

        project.afterEvaluate {
            check(extension.dimensions.isPresent)
            extension.sourceSetConfigureActions.disallowChanges()

            val sourceSetConfigurer = MultiDimensionSourceSetConfigurer(
                project = project,
                dimensions = extension.dimensions.get(),
                targetConfigurer = targetConfigurer,
                sourceSetConfigureActions = extension.sourceSetConfigureActions.get(),
            )

            sourceSetConfigurer.configure()
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
                    println("Removed dependencies from ${sourceSetPair.sourceSet.name}: $dependenciesToRemove")
                    dependencies.removeAll(dependenciesToRemove)
                    hasDoneWork = true
                }
            }
        } while (hasDoneWork)

        println("All dependencies:")
        println(
            allDependencies.toList().joinToString("\n") { (pair, dependencies) ->
                pair.sourceSet.name + dependencies.joinToString(prefix = "=> [", postfix = "]") { it.sourceSet.name }
            }
        )

        println("Direct dependencies:")
        println(
            directDependencies.toList().joinToString("\n") { (pair, dependencies) ->
                pair.sourceSet.name + dependencies.joinToString(prefix = "=> [", postfix = "]") { it.sourceSet.name }
            }
        )

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
