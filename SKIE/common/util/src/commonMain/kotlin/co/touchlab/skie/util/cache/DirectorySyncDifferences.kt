package co.touchlab.skie.util.cache

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

fun Path.syncDirectoryContentIfDifferent(destination: Path) {
    if (!destination.exists()) {
        destination.createDirectories()
    }

    require(this.isDirectory()) { "Source ${this.absolutePathString()} must be a directory." }
    require(destination.isDirectory()) { "Destination ${destination.absolutePathString()} must be a directory." }

    deleteRemovedFilesFromMirror(this, destination)
    deleteRemovedDirectoriesFromMirror(this, destination)

    copyChildFilesIfDifferent(this, destination)
    syncChildDirectoriesContentIfDifferent(this, destination)
}

private fun deleteRemovedFilesFromMirror(origin: Path, mirror: Path) {
    val originFiles = origin.listDirectoryEntries().filterNot { it.isDirectory() }.map { it.name }.toSet()
    val mirrorFiles = mirror.listDirectoryEntries().filterNot { it.isDirectory() }.map { it.name }.toSet()

    val removedFiles = mirrorFiles - originFiles

    removedFiles.forEach {
        mirror.resolve(it).deleteIfExists()
    }
}

private fun deleteRemovedDirectoriesFromMirror(origin: Path, mirror: Path) {
    val originDirectories = origin.listDirectoryEntries().filter { it.isDirectory() }.map { it.name }.toSet()
    val mirrorDirectories = mirror.listDirectoryEntries().filter { it.isDirectory() }.map { it.name }.toSet()

    val removedDirectories = mirrorDirectories - originDirectories

    removedDirectories.forEach {
        mirror.resolve(it).deleteRecursivelyIfExists()
    }
}

private fun copyChildFilesIfDifferent(origin: Path, destination: Path) {
    origin.listDirectoryEntries()
        .toList()
        .parallelStream()
        .filter { !it.isDirectory() }
        .forEach { file ->
            file.copyFileToIfDifferent(destination.resolve(file.name))
        }
}

private fun syncChildDirectoriesContentIfDifferent(origin: Path, destination: Path) {
    origin.listDirectoryEntries()
        .toList()
        .parallelStream()
        .filter { it.isDirectory() }
        .forEach { directory ->
            directory.syncDirectoryContentIfDifferent(destination.resolve(directory.name))
        }
}

// There is currently no stable API for this in Kotlin
private fun Path.deleteRecursivelyIfExists() {
    if (isDirectory()) {
        listDirectoryEntries().forEach {
            it.deleteRecursivelyIfExists()
        }
    }

    deleteIfExists()
}
