package co.touchlab.skie.util.cache

import java.io.File

fun File.writeTextIfDifferent(text: String) {
    require(isFile || !exists()) { "File $absolutePath must be either a regular file or not exist." }

    val existingContent = readTextOrNull()
    if (existingContent != text) {
        writeText(text)
    }
}
