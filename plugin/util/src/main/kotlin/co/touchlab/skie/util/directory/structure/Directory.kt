package co.touchlab.skie.util.directory.structure

import java.io.File

abstract class Directory(
    val parent: Directory?,
    val directory: File,
) {

    abstract val isTemporary: Boolean

    init {
        @Suppress("LeakingThis")
        parent?.addChild(this)

        directory.mkdirs()
    }

    abstract fun addChild(child: Directory)
}
