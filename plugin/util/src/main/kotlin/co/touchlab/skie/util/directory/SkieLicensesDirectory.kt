package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.util.initializedDirectory
import java.io.File

object SkieLicensesDirectory {

    val directory: File = SkieUserDataDirectories.applicationSupport
        .resolve("license")
        .initializedDirectory()
}

