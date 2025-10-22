package co.touchlab.skie.sir.element

import java.nio.file.Path

// Instantiate only in SirFileProvider
class SirCompilableFile(
    override val module: SirModule.Skie,
    val absolutePath: Path,
    // Relative to the SKIE Swift build directory
    val relativePath: Path,
    val originFile: SirSourceFile?,
) : SirFile {

    init {
        module.files.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $absolutePath"
}
