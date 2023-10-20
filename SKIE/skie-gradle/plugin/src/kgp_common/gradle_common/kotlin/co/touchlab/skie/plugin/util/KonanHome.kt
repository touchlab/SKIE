package co.touchlab.skie.plugin.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

internal fun Project.getKonanHome(): File =
    NativeCompilerDownloader(this).compilerDirectory
