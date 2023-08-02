package co.touchlab.skie.util.directory

import java.io.File

class SkieDirectories(
    skieBuildRootDirectory: File,
) {

    val buildDirectory: SkieBuildDirectory = SkieBuildDirectory(skieBuildRootDirectory)

    val applicationSupport: SkieApplicationSupportDirectory =
        File(System.getProperty("user.home"))
            .resolve("Library/Application Support/SKIE")
            .let { SkieApplicationSupportDirectory(it) }
}
