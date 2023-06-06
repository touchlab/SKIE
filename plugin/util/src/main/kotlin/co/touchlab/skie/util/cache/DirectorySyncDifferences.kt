package co.touchlab.skie.util.cache

import java.io.File

fun File.syncDirectoryContentIfDifferent(destination: File) {
    if (!destination.exists()) {
        destination.mkdirs()
    }

    require(isDirectory) { "Source $absolutePath must be a directory." }
    require(destination.isDirectory) { "Destination ${destination.absolutePath} must be a directory." }

    deleteRemovedFilesFromMirror(this, destination)
    deleteRemovedDirectoriesFromMirror(this, destination)

    copyChildFilesIfDifferent(this, destination)
    syncChildDirectoriesContentIfDifferent(this, destination)
}

private fun deleteRemovedFilesFromMirror(origin: File, mirror: File) {
    val originFiles = origin.listFiles()?.filterNot { it.isDirectory }?.map { it.name }?.toSet() ?: emptySet()
    val mirrorFiles = mirror.listFiles()?.filterNot { it.isDirectory }?.map { it.name }?.toSet() ?: emptySet()

    val removedFiles = mirrorFiles - originFiles

    removedFiles.forEach {
        mirror.resolve(it).delete()
    }
}

private fun deleteRemovedDirectoriesFromMirror(origin: File, mirror: File) {
    val originDirectories = origin.listFiles()?.filter { it.isDirectory }?.map { it.name }?.toSet() ?: emptySet()
    val mirrorDirectories = mirror.listFiles()?.filter { it.isDirectory }?.map { it.name }?.toSet() ?: emptySet()

    val removedDirectories = mirrorDirectories - originDirectories

    removedDirectories.forEach {
        mirror.resolve(it).deleteRecursively()
    }
}

private fun copyChildFilesIfDifferent(origin: File, destination: File) {
    origin.listFiles()
        ?.toList()
        ?.parallelStream()
        ?.filter { !it.isDirectory }
        ?.forEach { file ->
            file.copyFileToIfDifferent(destination.resolve(file.name))
        }
}

private fun syncChildDirectoriesContentIfDifferent(origin: File, destination: File) {
    origin.listFiles()
        ?.toList()
        ?.parallelStream()
        ?.filter { it.isDirectory }
        ?.forEach { directory ->
            directory.syncDirectoryContentIfDifferent(destination.resolve(directory.name))
        }
}
