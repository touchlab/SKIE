package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.util.SkieTask
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieBuildDirectory
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal object SwiftLinkingConfigurator {

    fun configureUserSwiftLinking(linkTask: KotlinNativeLink, analyticsManager: GradleAnalyticsManager) {
        with(linkTask.project) {
            val swiftSourceSets = linkTask.binary.compilation.allKotlinSourceSets.map { createSwiftSourceSet(it) }

            val swiftSources = objects.fileCollection().from(swiftSourceSets)

            registerCopyTask(linkTask, analyticsManager, swiftSources)
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

    private fun registerCopyTask(
        linkTask: KotlinNativeLink,
        analyticsManager: GradleAnalyticsManager,
        swiftSources: ConfigurableFileCollection?,
    ) {
        val syncTask = linkTask.registerSkieLinkBasedTask<SkieSyncTask>("copySwift", analyticsManager) {
            from(swiftSources)

            into(linkTask.skieBuildDirectory.swift.custom.directory)
        }

        linkTask.inputs.files(syncTask.map { it.outputs.files })
    }

    abstract class SkieSyncTask : Sync(), SkieTask {

        override fun runTask() {
        }
    }
}
