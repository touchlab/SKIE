package co.touchlab.skie.util.directory.util

import java.io.File

fun File.initializedDirectory(): File {
    mkdirs()

    return this
}
