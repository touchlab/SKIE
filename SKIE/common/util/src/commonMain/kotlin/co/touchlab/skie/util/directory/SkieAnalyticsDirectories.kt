package co.touchlab.skie.util.directory

import co.touchlab.skie.util.Environment
import co.touchlab.skie.util.directory.util.initializedDirectory
import java.io.File

class SkieAnalyticsDirectories(
    skieBuildDirectory: SkieBuildDirectory,
) {

    private val debug: File = skieBuildDirectory.debug.analytics.directory

    private val release: File =
        SkieUserDataDirectories.applicationSupport
            .resolve("analytics")
            .initializedDirectory()

    val directoryWithFilesToUpload: File = release

    fun forEnvironment(environment: Environment): List<File> =
        when (environment) {
            Environment.Production -> listOf(release)
            Environment.Dev -> listOf(debug, release)
            Environment.Tests -> listOf(debug)
            Environment.Unknown -> listOf(release)
        }
}
