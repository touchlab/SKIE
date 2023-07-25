package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.directory.skieBuildDirectory
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

internal object SwiftLinkingConfigurator {

    fun configureCustomSwiftLinking(linkTask: KotlinNativeLink) {
        with(linkTask.project) {
            val swiftSourceSets = linkTask.binary.compilation.allKotlinSourceSets.map { createSwiftSourceSet(it) }

            val swiftSources = objects.fileCollection().from(swiftSourceSets)

            registerCustomSwiftTasks(linkTask, swiftSources)
        }
    }

    private fun Project.createSwiftSourceSet(kotlinSourceSet: KotlinSourceSet): SourceDirectorySet {
        val swiftSourceSetName = "${kotlinSourceSet.name} Swift source"

        return objects.sourceDirectorySet(swiftSourceSetName, swiftSourceSetName).apply {
            filter.include("**/*.swift")
            srcDirs(kotlinSourceSet.swiftSourceDirectory)
        }
    }

    private val KotlinSourceSet.swiftSourceDirectory: String
        get() = "src/$name/swift"

    private fun registerCustomSwiftTasks(
        linkTask: KotlinNativeLink,
        swiftSources: ConfigurableFileCollection?,
    ) {
        val mergeTask = linkTask.registerSkieLinkBasedTask<SkieMergeCustomSwiftTask>("mergeCustomSwift") {
            from(swiftSources)

            into(linkTask.skieBuildDirectory.temp.gradle.mergedCustomSwift.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        val syncTask = linkTask.registerSkieLinkBasedTask<SkiePackageCustomSwiftTask>("packageCustomSwift") {
            inputDirectory.set(mergeTask.map { it.outputs.files.singleFile })

            outputDirectory.set(linkTask.skieBuildDirectory.swift.custom.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.inputs.files(syncTask.map { it.outputs.files })
    }

    abstract class SkieMergeCustomSwiftTask : Sync() {

        @TaskAction
        fun runTask() {
            destinationDir.mkdirs()
        }
    }

    abstract class SkiePackageCustomSwiftTask : DefaultTask() {

        @get:InputDirectory
        abstract val inputDirectory: Property<File>

        @get:OutputDirectory
        abstract val outputDirectory: Property<File>

        @TaskAction
        fun runTask() {
            inputDirectory.get().syncDirectoryContentIfDifferent(outputDirectory.get())
        }
    }
}
