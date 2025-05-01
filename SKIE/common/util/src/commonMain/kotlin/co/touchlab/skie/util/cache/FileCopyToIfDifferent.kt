package co.touchlab.skie.util.cache

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeText

fun File.copyFileToIfDifferent(destination: File): Boolean = toPath().copyFileToIfDifferent(destination.toPath())

fun Path.copyFileToIfDifferent(destination: Path): Boolean {
    require(isRegularFile() || !exists()) { "Source ${absolutePathString()} must be either a regular file or not exist." }
    require(destination.isRegularFile() || !destination.exists()) {
        "Destination ${destination.absolutePathString()} must be either a regular file or not exist."
    }

    val sourceContent = if (exists()) readText() else null
    val destinationContent = if (destination.exists()) destination.readText() else null

    return if (sourceContent != null) {
        if (sourceContent != destinationContent) {
            destination.writeText(sourceContent)

            true
        } else {
            false
        }
    } else {
        if (destinationContent != null) {
            destination.deleteIfExists()

            true
        } else {
            false
        }
    }
}
