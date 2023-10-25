package co.touchlab.skie.sir.element

import java.nio.file.Path

// Instantiate only in SirProvider
class SirFile(
    val namespace: String,
    val name: String,
    override val module: SirModule.Skie,
) : SirElement, SirTopLevelDeclarationParent {

    val imports: MutableList<String> = mutableListOf()

    // Relative to generated swift directory
    val relativePath: Path
        get() = relativePath(namespace, name)

    var content: String = ""

    override val parent: SirModule
        get() = module

    init {
        module.files.add(this)
    }

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: $namespace.$name.swift"

    companion object {

        fun relativePath(namespace: String, name: String): Path =
            Path.of("$namespace/$namespace.$name.swift")
    }
}
