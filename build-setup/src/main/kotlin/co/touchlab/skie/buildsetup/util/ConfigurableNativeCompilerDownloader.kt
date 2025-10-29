package co.touchlab.skie.buildsetup.util

import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.util.DependencyDirectories
import java.io.File
import java.nio.file.Files

// Based on org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
class CustomizableNativeCompilerDownloader(
    private val project: Project,
    private val kotlinVersion: String,
) {

    private val simpleOsName = HostManager.platformName()

    private val dependencyNameWithOsAndVersion: String = "kotlin-native-prebuilt-$simpleOsName-$kotlinVersion"

    val compilerDirectory: File
        get() = DependencyDirectories
            .getLocalKonanDir()
            .resolve(dependencyNameWithOsAndVersion)

    fun downloadIfNeeded() {
        if (!compilerDirectory.exists()) {
            downloadAndExtract()
        }
    }

    private fun downloadAndExtract() {
        val dependencyNotation = mapOf(
            "group" to "org.jetbrains.kotlin",
            "name" to "kotlin-native-prebuilt",
            "version" to kotlinVersion,
            "classifier" to simpleOsName,
            "ext" to "tar.gz",
        )

        val compilerDependency = project.dependencies.create(dependencyNotation)

        val configuration = project.configurations.detachedConfiguration(compilerDependency)

        val archive = configuration.files.single()

        extractKotlinNativeFromArchive(archive)
    }

    private fun extractKotlinNativeFromArchive(archive: File) {
        val kotlinNativeDir = compilerDirectory.parentFile.also { it.mkdirs() }
        val tmpDir = Files.createTempDirectory(kotlinNativeDir.toPath(), "compiler-").toFile()
        try {
            project.copy {
                from(project.tarTree(archive))
                into(tmpDir)
            }
            val compilerTmp = tmpDir.resolve(dependencyNameWithOsAndVersion)
            if (!compilerTmp.renameTo(compilerDirectory)) {
                project.copy {
                    from(compilerTmp)
                    into(compilerDirectory)
                }
            }
        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
