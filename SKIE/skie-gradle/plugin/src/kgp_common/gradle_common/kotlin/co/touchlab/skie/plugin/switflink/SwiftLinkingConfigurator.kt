package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.util.BaseSkieTask
import co.touchlab.skie.plugin.util.SkieTask
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.directory.skieBuildDirectory
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import java.io.File

internal object SwiftLinkingConfigurator {

    fun configureCustomSwiftLinking(linkTask: KotlinNativeLink, analyticsManager: GradleAnalyticsManager) {
        with(linkTask.project) {
            val swiftSourceSets = linkTask.binary.compilation.allKotlinSourceSets.map { createSwiftSourceSet(it) }

            val swiftSources = objects.fileCollection().from(swiftSourceSets)

            registerCustomSwiftTasks(linkTask, analyticsManager, swiftSources)
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
        analyticsManager: GradleAnalyticsManager,
        swiftSources: ConfigurableFileCollection?,
    ) {
        val mergeTask = linkTask.registerSkieLinkBasedTask<SkieMergeCustomSwiftTask>("mergeCustomSwift", analyticsManager) {
            from(swiftSources)

            into(linkTask.skieBuildDirectory.temp.gradle.mergedCustomSwift.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        val syncTask = linkTask.registerSkieLinkBasedTask<SkiePackageCustomSwiftTask>("packageCustomSwift", analyticsManager) {
            inputDirectory.set(mergeTask.map { it.outputs.files.singleFile })

            outputDirectory.set(linkTask.skieBuildDirectory.swift.custom.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.inputs.files(syncTask.map { it.outputs.files })
    }

    abstract class SkieMergeCustomSwiftTask : Sync(), SkieTask {

        override fun runTask() {
        }
    }

    abstract class SkiePackageCustomSwiftTask : BaseSkieTask() {

        @get:InputDirectory
        abstract val inputDirectory: Property<File>

        @get:OutputDirectory
        abstract val outputDirectory: Property<File>

        override fun runTask() {
            inputDirectory.get().syncDirectoryContentIfDifferent(outputDirectory.get())
        }
    }
}
