package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.shim.ShimEntrypoint
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import java.io.File

internal fun Project.getKonanHome(shims: ShimEntrypoint): File =
    shims.nativeCompilerDownloaderProvider.provide(this).compilerDirectory
