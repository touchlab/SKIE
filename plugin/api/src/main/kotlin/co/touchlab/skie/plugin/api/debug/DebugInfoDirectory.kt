package co.touchlab.skie.plugin.api.debug

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class DebugInfoDirectory(
    private val directory: File,
) {
    init {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    val logs: Subdirectory by lazy {
        Subdirectory(directory.resolve("logs"))
    }

    val dumps: Subdirectory by lazy {
        Subdirectory(directory.resolve("dumps"))
    }

    class Subdirectory(
        private val directory: File
    ) {
        init {
            directory.mkdirs()
        }

        fun resolve(name: String): File {
            return directory.resolve(name)
        }
    }
}
