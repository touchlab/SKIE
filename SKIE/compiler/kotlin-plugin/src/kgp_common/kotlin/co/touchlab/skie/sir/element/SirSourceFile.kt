package co.touchlab.skie.sir.element

import java.nio.file.Path

// Instantiate only in SirFileProvider
class SirSourceFile(
    override val module: SirModule.Skie,
    // Relative to the SKIE Swift generated directory
    val relativePath: Path,
    val originFile: SirIrFile? = null
) : SirFile {

    var content: String = ""

    init {
        module.files.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $relativePath"
}
