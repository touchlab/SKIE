package co.touchlab.skie.plugin.util

import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems

fun File.writeToZip(write: (FileSystem) -> Unit) {
    val fileUri = this.toURI()

    // Solves an issue with spaces in the path
    val uri = URI("jar:file", fileUri.userInfo, fileUri.host, fileUri.port, fileUri.path, fileUri.query, fileUri.fragment)

    val fileSystem = try {
        FileSystems.getFileSystem(uri)
    } catch (_: FileSystemNotFoundException) {
        FileSystems.newFileSystem(uri, mapOf("create" to true))
    }

    fileSystem.use(write)
}
