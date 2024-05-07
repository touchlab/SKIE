package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import co.touchlab.skie.plugin.skieBuildDirectory
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
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
import java.io.File

internal object SwiftLinkingConfigurator {

    fun configureCustomSwiftLinking(target: SkieTarget) {
        // TODO: Change this so that it bundles Swift directly to klib
        if (target is SkieTarget.Binary) {
            with(target.project) {
                val swiftSourceSets = target.allCompilationKotlinSourceSets.map { createSwiftSourceSet(it) }

                val swiftSources = objects.fileCollection().from(swiftSourceSets)

                registerCustomSwiftTasks(target, swiftSources)
            }
        }
    }

    private fun Project.createSwiftSourceSet(kotlinSourceSet: KotlinSourceSetShim): SourceDirectorySet {
        val swiftSourceSetName = "${kotlinSourceSet.name} Swift source"

        return objects.sourceDirectorySet(swiftSourceSetName, swiftSourceSetName).apply {
            filter.include("**/*.swift")
            srcDirs(kotlinSourceSet.swiftSourceDirectory)
        }
    }

    private val KotlinSourceSetShim.swiftSourceDirectory: String
        get() = "src/$name/swift"

    private fun registerCustomSwiftTasks(
        target: SkieTarget.Binary,
        swiftSources: ConfigurableFileCollection?,
    ) {
        val mergeTask = target.registerSkieTargetBasedTask<SkieMergeCustomSwiftTask>("mergeCustomSwift") {
            from(swiftSources)

            into(target.skieBuildDirectory.map { it.temp.gradle.mergedCustomSwift.directory })

            dependsOn(target.createSkieBuildDirectoryTask)
        }

        val syncTask = target.registerSkieTargetBasedTask<SkiePackageCustomSwiftTask>("packageCustomSwift") {
            inputDirectory.set(mergeTask.map { it.outputs.files.singleFile })

            outputDirectory.set(target.skieBuildDirectory.map { it.swift.custom.directory })

            dependsOn(target.createSkieBuildDirectoryTask)
        }

        target.task.configure {
            inputs.files(syncTask.map { it.outputs.files })
        }
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
