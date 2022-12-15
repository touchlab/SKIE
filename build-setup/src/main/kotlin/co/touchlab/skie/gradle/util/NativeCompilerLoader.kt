package co.touchlab.skie.gradle.util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

fun Project.kotlinNativeCompilerEmbeddableRuntime(): FileCollection =
    NativeCompilerDownloader(project)
        .also { it.downloadIfNeeded() }
        .compilerDirectory
        .resolve("konan/lib/kotlin-native-compiler-embeddable.jar")
        .let { files(it) }
