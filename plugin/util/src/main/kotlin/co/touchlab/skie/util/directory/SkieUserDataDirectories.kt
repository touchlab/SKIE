package co.touchlab.skie.util.directory

import co.touchlab.skie.util.BuildConfig
import co.touchlab.skie.util.directory.util.initializedDirectory
import java.io.File

object SkieUserDataDirectories {

    val applicationSupport: File =
        File(System.getProperty("user.home"))
            .resolve("Library/Application Support/SKIE/${BuildConfig.SKIE_VERSION}")
            .initializedDirectory()
}
