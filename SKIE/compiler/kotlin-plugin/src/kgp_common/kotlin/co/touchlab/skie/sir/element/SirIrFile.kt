package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFileProvider
import java.nio.file.Path

// Instantiate only in SirFileProvider
class SirIrFile(
    val namespace: String,
    val name: String,
    override val module: SirModule.Skie,
) : SirFile, SirTopLevelDeclarationParent {

    // Relative to the SKIE Swift generated directory
    val relativePath: Path
        get() = SirFileProvider.relativePath(namespace, name)

    val imports: MutableList<String> = mutableListOf()

    override val parent: SirDeclarationParent?
        get() = null

    init {
        module.files.add(this)
    }

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: $namespace.$name.swift"
}
