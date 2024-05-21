package co.touchlab.skie.plugin.util

import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems

fun File.writeToZip(write: (FileSystem) -> Unit) {
    val uri = URI.create("jar:file:" + this.absolutePath)

    val fileSystem = try {
        FileSystems.getFileSystem(uri)
    } catch (_: FileSystemNotFoundException) {
        FileSystems.newFileSystem(uri, mapOf("create" to true))
    }

    fileSystem.use(write)
}
