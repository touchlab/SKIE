package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.shim.KotlinNativeCompilationShim
import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import co.touchlab.skie.plugin.util.lowerCamelCaseName
import co.touchlab.skie.plugin.util.registerSkieTask
import co.touchlab.skie.plugin.util.writeToZip
import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import java.io.File

object SwiftBundlingConfigurator {

    private const val klibSkieSwiftDirectory: String = "default/skie/swift"

    fun configureCustomSwiftBundling(project: Project) {
        project.kgpShim.kotlinNativeTargets.configureEach {
            compilations.configureEach {
                project.configureSwiftBundlingToKlib(this)
            }
        }
    }

    private fun Project.configureSwiftBundlingToKlib(compilation: KotlinNativeCompilationShim) {
        val processSwiftSourcesTaskProvider = createProcessSwiftSourcesTask(compilation)

        compilation.configureCompileTask(processSwiftSourcesTaskProvider)
    }

    private fun Project.createProcessSwiftSourcesTask(compilation: KotlinNativeCompilationShim): Provider<ProcessSwiftSourcesTask> {
        val compilationPrefix = compilation.name.takeIf { it.lowercase() != "main" }

        val baseName = lowerCamelCaseName("process", compilationPrefix, compilation.target.name, "swiftSources")

        val swiftSourceSet = createSwiftSourceSet(compilation)

        return registerSkieTask<ProcessSwiftSourcesTask>(baseName) {
            inputs.files(swiftSourceSet)
            output.set(compilation.skieCompilationDirectory.map { it.swift.bundled.directory })
        }
    }

    private fun Project.createSwiftSourceSet(compilation: KotlinNativeCompilationShim): SourceDirectorySet {
        val swiftSourceSetName = "${compilation.target.name}:${compilation.name} Swift sources"

        val swiftSourceSet = objects.sourceDirectorySet(swiftSourceSetName, swiftSourceSetName).apply {
            filter.include("**/*.swift")
        }

        compilation.allKotlinSourceSets.configureEach {
            val swiftDirectory = project.layout.projectDirectory.dir(swiftSourceDirectory)

            swiftSourceSet.srcDirs(swiftDirectory)
        }

        return swiftSourceSet
    }

    private fun KotlinNativeCompilationShim.configureCompileTask(processSwiftSourcesTaskProvider: Provider<ProcessSwiftSourcesTask>) {
        compileTaskProvider.configure {
            inputs.files(processSwiftSourcesTaskProvider.map { it.outputs })

            doLast {
                copySwiftSourcesToKlib(compileTaskOutputFileProvider.get(), processSwiftSourcesTaskProvider.get())
            }
        }
    }

    private fun copySwiftSourcesToKlib(klib: File, processResourcesTask: ProcessSwiftSourcesTask) {
        val swiftSourcesDirectory = processResourcesTask.output.get()

        if (!swiftSourcesDirectory.exists()) {
            return
        }

        klib.writeToZip { fileSystem ->
            val klibSwiftSourcesDirectory = fileSystem.getPath("/$klibSkieSwiftDirectory")

            swiftSourcesDirectory.toPath().syncDirectoryContentIfDifferent(klibSwiftSourcesDirectory)
        }
    }

    // TODO Allow users to customize the swift source directory path
    private val KotlinSourceSetShim.swiftSourceDirectory: String
        get() = "src/$name/swift"
}
