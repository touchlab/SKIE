package co.touchlab.skie.gradle.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

val Project.kotlinNativeCompilerHome: File
    get() = NativeCompilerDownloader(project)
        .also { it.downloadIfNeeded() }
        .compilerDirectory
