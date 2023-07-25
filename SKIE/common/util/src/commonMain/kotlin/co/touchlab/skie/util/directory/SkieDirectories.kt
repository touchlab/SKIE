package co.touchlab.skie.util.directory

import java.io.File

class SkieDirectories(
    skieBuildRootDirectory: File,
) {

    val buildDirectory: SkieBuildDirectory = SkieBuildDirectory(skieBuildRootDirectory)
}
