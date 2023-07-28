package co.touchlab.skie.gradle.version.target

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

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
        val isMain: Boolean
            get() = this is Main
        val isTest: Boolean
            get() = this is Test

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
