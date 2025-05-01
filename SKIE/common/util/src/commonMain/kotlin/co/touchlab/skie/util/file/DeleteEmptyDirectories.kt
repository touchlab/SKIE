package co.touchlab.skie.util.file

import java.io.File

fun File.deleteEmptyDirectoriesRecursively() {
    if (!isDirectory) {
        return
    }

    listFiles()?.forEach {
        it.deleteEmptyDirectoriesRecursively()
    }

    if (listFiles()?.isEmpty() == true) {
        delete()
    }
}
