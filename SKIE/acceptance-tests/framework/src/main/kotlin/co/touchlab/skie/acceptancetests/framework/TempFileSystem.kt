package co.touchlab.skie.acceptancetests.framework

import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists

class TempFileSystem(private val tempDirectory: Path) {

    private val createdFiles = mutableListOf<Path>()

    init {
        tempDirectory.toFile().deleteRecursively()
        tempDirectory.createDirectories()
    }

    fun createFile(name: String): Path {
        val file = tempDirectory.resolve(name)

        if (file.exists()) {
            return createFile("${name}_${name.hashCode()}")
        }

        return file.createFile()
            .also {
                createdFiles.add(it)
            }
    }

    fun createDirectory(name: String): Path {
        val directory = tempDirectory.resolve(name)

        if (directory.exists()) {
            return createDirectory("${name}_${name.hashCode()}")
        }

        return directory.createDirectory()
            .also {
                createdFiles.add(it)
            }
    }

    fun describeCreatedFiles(filter: (File) -> Boolean): String {
        val tempDirectoryFile = tempDirectory.toFile()

        return tempDirectoryFile.walk(FileWalkDirection.TOP_DOWN)
            .filter { it.isFile }
            .filter(filter)
            .joinToString("\n") {
                it.relativeTo(tempDirectoryFile).path + ": " + it.absolutePath
            }
    }

    fun deleteCreatedFiles() {
        if ("keepTemporaryFiles" in TestProperties) {
            return
        }

        createdFiles.forEach {
            it.toFile().deleteRecursively()
        }

        createdFiles.clear()
    }
}
