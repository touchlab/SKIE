package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieBuildDirectory
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal object SwiftLinkingConfigurator {

    fun configureUserSwiftLinking(linkTask: KotlinNativeLink) {
        with(linkTask.project) {
            val swiftSourceSets = linkTask.binary.compilation.allKotlinSourceSets.map { createSwiftSourceSet(it) }

            val swiftSources = objects.fileCollection().from(swiftSourceSets)

            registerCopyTask(linkTask, swiftSources)
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
        swiftSources: ConfigurableFileCollection?,
    ) {
        val syncTask = linkTask.registerSkieLinkBasedTask<Sync>("copySwift") {
            from(swiftSources)

            into(linkTask.skieBuildDirectory.swift.custom.directory)
        }

        linkTask.inputs.files(syncTask.map { it.outputs.files })
    }
}
