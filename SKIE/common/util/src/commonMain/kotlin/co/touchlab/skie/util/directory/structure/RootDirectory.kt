package co.touchlab.skie.util.directory.structure

import java.io.File

abstract class RootDirectory(directory: File) : Directory(null, directory) {

    override val isTemporary: Boolean = false

    private val allChildren: MutableList<Directory> = mutableListOf()

    fun createDirectories() {
        allChildren.forEach {
            it.directory.mkdirs()
        }
    }

    fun resetTemporaryDirectories() {
        allChildren
            .filter { it.isTemporary }
            .forEach {
                it.directory.deleteRecursively()
                it.directory.mkdirs()
            }
    }

    override fun addChild(child: Directory) {
        allChildren.add(child)
    }
}
