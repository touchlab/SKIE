package co.touchlab.skie.util.directory

import co.touchlab.skie.util.SystemProperty
import co.touchlab.skie.util.directory.structure.RootDirectory
import java.io.File

class SkieDirectories(
    skieBuildRootDirectory: File,
) {

    private val rootDirectories = mutableListOf<RootDirectory>()

    val buildDirectory: SkieBuildDirectory = rootDirectory {
        SkieBuildDirectory(skieBuildRootDirectory)
    }

    val applicationSupport: SkieApplicationSupportDirectory = rootDirectory {
        File(SystemProperty.get("user.home"))
            .resolve("Library/Application Support/SKIE")
            .let { SkieApplicationSupportDirectory(it) }
    }

    val directories: List<File>
        get() = rootDirectories.map { it.directory }

    private fun <T : RootDirectory> rootDirectory(action: () -> T): T =
        action().also { rootDirectories.add(it) }

    fun createDirectories() {
        rootDirectories.forEach {
            it.createDirectories()
        }
    }

    fun resetTemporaryDirectories() {
        rootDirectories.forEach {
            it.resetTemporaryDirectories()
        }
    }
}
