package co.touchlab.skie.util.directory

import java.io.File

class SkieDirectories(
    skieBuildRootDirectory: File,
) {

    val buildDirectory: SkieBuildDirectory = SkieBuildDirectory(skieBuildRootDirectory)

    val analyticsDirectories: SkieAnalyticsDirectories = SkieAnalyticsDirectories(buildDirectory)

    fun resetTemporaryDirectories() {
        buildDirectory.resetTemporaryDirectories()
    }
}
