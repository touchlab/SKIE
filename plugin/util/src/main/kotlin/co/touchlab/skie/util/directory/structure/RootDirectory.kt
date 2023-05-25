package co.touchlab.skie.util.directory.structure

import java.io.File

abstract class RootDirectory(
    directory: File,
) : Directory(null, directory) {

    override val isTemporary: Boolean = false

    private val children: MutableList<Directory> = mutableListOf()

    fun resetTemporaryDirectories() {
        children
            .filter { it.isTemporary }
            .forEach {
                it.directory.deleteRecursively()
                it.directory.mkdirs()
            }
    }

    override fun addChild(child: Directory) {
        children.add(child)
    }
}
