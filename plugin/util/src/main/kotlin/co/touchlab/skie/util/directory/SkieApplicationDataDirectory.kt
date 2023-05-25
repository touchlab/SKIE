package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.util.initializedDirectory
import java.io.File
import java.nio.file.Path
import co.touchlab.skie.util.BuildConfig

object SkieApplicationDataDirectory {

    val directory: File =
        Path.of(System.getProperty("user.home"))
            .resolve("Library/Application Support/SKIE/${BuildConfig.SKIE_VERSION}}")
            .toFile()
            .initializedDirectory()
}
