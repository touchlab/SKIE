package co.touchlab.skie.acceptancetests.framework

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
}
