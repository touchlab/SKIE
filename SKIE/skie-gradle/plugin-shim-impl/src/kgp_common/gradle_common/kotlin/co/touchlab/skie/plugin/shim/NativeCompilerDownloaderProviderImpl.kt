package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader

object NativeCompilerDownloaderProviderImpl : NativeCompilerDownloaderProvider {

    override fun provide(project: Project): NativeCompilerDownloader {
        return NativeCompilerDownloader(project)
    }
}
