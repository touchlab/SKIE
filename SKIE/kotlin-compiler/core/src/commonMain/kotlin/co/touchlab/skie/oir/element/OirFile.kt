package co.touchlab.skie.oir.element

import java.nio.file.Path

// Instantiate only in OirProvider
class OirFile(val name: String, override val module: OirModule.Kotlin) :
    OirElement,
    OirTopLevelDeclarationParent {

    val imports: MutableList<String> = mutableListOf()

    // Relative to header directory
    val relativePath: Path
        get() = relativePath(module.name, name)

    var headerContent: String = ""

    init {
        module.files.add(this)
    }

    override val declarations: MutableList<OirTopLevelDeclaration> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: ${module.name}.$name.h"

    companion object {

        fun relativePath(namespace: String, name: String): Path = Path.of("$namespace/$namespace.$name.h")
    }
}
