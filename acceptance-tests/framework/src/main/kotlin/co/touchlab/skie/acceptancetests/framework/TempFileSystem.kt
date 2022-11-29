package co.touchlab.skie.acceptancetests.framework

import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

class TempFileSystem(private val tempDirectory: Path) {

    init {
        tempDirectory.toFile().deleteRecursively()
        tempDirectory.createDirectories()
    }

    fun createFile(name: String): Path =
        tempDirectory.resolve(name).createFile()

    fun createDirectory(name: String): Path =
        tempDirectory.resolve(name).createDirectory()

    fun describeCreatedFiles(filter: (File) -> Boolean): String {
        val tempDirectoryFile = tempDirectory.toFile()

        return tempDirectoryFile.walk(FileWalkDirection.TOP_DOWN)
            .filter { it.isFile }
            .filter(filter)
            .joinToString("\n") {
                it.relativeTo(tempDirectoryFile).path + ": " + it.absolutePath
            }
    }
}
