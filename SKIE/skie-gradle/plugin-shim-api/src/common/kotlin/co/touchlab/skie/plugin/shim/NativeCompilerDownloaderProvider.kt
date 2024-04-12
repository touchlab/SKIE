package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader

interface NativeCompilerDownloaderProvider {
    fun provide(project: Project): NativeCompilerDownloader
}
