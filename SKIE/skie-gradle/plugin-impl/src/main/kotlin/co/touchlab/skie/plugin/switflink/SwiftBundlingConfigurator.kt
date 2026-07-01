package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.shim.KotlinNativeCompilationShim
import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.lowerCamelCaseName
import co.touchlab.skie.plugin.util.registerSkieTask
import co.touchlab.skie.plugin.util.writeToZip
import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import co.touchlab.skie.util.file.isKlib
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import java.io.File

object SwiftBundlingConfigurator {

    const val KLIB_SKIE_SWIFT_DIRECTORY: String = "default/skie/swift"

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

        val baseName = lowerCamelCaseName("processSwiftSources", compilationPrefix, compilation.target.name)

        val swiftSourceFiles = createSwiftSourceFiles(compilation)

        val isSwiftBundlingEnabledProperty = skieExtension.swiftBundling.enabled

        return registerSkieTask<ProcessSwiftSourcesTask>(baseName) {
            inputs.files(swiftSourceFiles)
            output.set(compilation.skieCompilationDirectory.map { it.swift.bundled.directory })

            onlyIf {
                isSwiftBundlingEnabledProperty.get()
            }
        }
    }

    private fun Project.createSwiftSourceFiles(compilation: KotlinNativeCompilationShim): FileCollection {
        // A ConfigurableFileCollection populated with concrete directories is serializable by the
        // Gradle configuration cache (it stores resolved paths, not the source set container or its
        // actions). Using a SourceDirectorySet here instead captures a non-serializable
        // DefaultSourceDirectorySet into the task graph and discards the configuration cache entry.
        val swiftSourceFiles = objects.fileCollection()

        compilation.allKotlinSourceSets.all {
            swiftSourceFiles.from(layout.projectDirectory.dir(swiftSourceDirectory))
        }

        // Preserve the previous filter.include("**/*.swift") behavior for up-to-date checks.
        return swiftSourceFiles.asFileTree.matching { include("**/*.swift") }
    }

    private fun KotlinNativeCompilationShim.configureCompileTask(processSwiftSourcesTaskProvider: Provider<ProcessSwiftSourcesTask>) {
        val processSwiftSourcesTaskOutput = processSwiftSourcesTaskProvider.flatMap { it.output }

        val compileTaskOutputFileProvider = compileTaskOutputFileProvider

        compileTaskProvider.configure {
            inputs.files(processSwiftSourcesTaskOutput)

            doLastOptimized {
                copySwiftSourcesToKlib(compileTaskOutputFileProvider.get(), processSwiftSourcesTaskOutput)
            }
        }
    }

    private fun copySwiftSourcesToKlib(klib: File, processSwiftSourcesTaskOutput: Provider<File>) {
        val swiftSourcesDirectory = processSwiftSourcesTaskOutput.get()

        if (!swiftSourcesDirectory.exists()) {
            return
        }

        if (klib.isKlib) {
            klib.writeToZip { fileSystem ->
                val klibSwiftSourcesDirectory = fileSystem.getPath("/$KLIB_SKIE_SWIFT_DIRECTORY")

                swiftSourcesDirectory.toPath().syncDirectoryContentIfDifferent(klibSwiftSourcesDirectory)
            }
        } else {
            swiftSourcesDirectory.toPath().syncDirectoryContentIfDifferent(klib.toPath().resolve(KLIB_SKIE_SWIFT_DIRECTORY))
        }
    }

    // TODO Allow users to customize the swift source directory path
    private val KotlinSourceSetShim.swiftSourceDirectory: String
        get() = "src/$name/swift"
}
