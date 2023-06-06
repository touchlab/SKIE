package co.touchlab.skie.util.cache

import java.io.File

fun File.copyFileToIfDifferent(destination: File): Boolean {
    require(isFile || !exists()) { "Source $absolutePath must be either a regular file or not exist." }
    require(destination.isFile || !destination.exists()) { "Destination ${destination.absolutePath} must be either a regular file or not exist." }

    val sourceContent = readTextOrNull()
    val destinationContent = destination.readTextOrNull()

    return if (sourceContent != null) {
        if (sourceContent != destinationContent) {
            destination.writeText(sourceContent)

            true
        } else {
            false
        }
    } else {
        if (destinationContent != null) {
            destination.delete()

            true
        } else {
            false
        }
    }
}
