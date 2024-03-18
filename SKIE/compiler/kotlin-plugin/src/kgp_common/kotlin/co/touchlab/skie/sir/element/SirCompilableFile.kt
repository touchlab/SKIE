package co.touchlab.skie.sir.element

import java.nio.file.Path

// Instantiate only in SirFileProvider
class SirCompilableFile(
    val originFile: SirSourceFile,
    val absolutePath: Path,
) : SirFile {

    override val module: SirModule.Skie
        get() = originFile.module

    init {
        module.files.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $absolutePath"
}
