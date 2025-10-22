package co.touchlab.skie.util.directory.structure

import java.io.File
import java.nio.file.Path

abstract class Directory(
    val parent: Directory?,
    val directory: File,
) {

    abstract val isTemporary: Boolean

    val path: Path = directory.toPath()

    init {
        @Suppress("LeakingThis")
        parent?.addChild(this)
    }

    abstract fun addChild(child: Directory)
}
