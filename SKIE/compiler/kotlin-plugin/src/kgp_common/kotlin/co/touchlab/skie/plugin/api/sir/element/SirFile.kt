package co.touchlab.skie.plugin.api.sir.element

import io.outfoxx.swiftpoet.FileSpec
import java.nio.file.Path

// File can only be created from SirProvider
class SirFile(
    val namespace: String,
    val name: String,
    override val module: SirModule.Skie,
) : SirElement, SirTopLevelDeclarationParent {

    // Relative to generated swift directory
    val relativePath: Path
        get() = relativePath(namespace, name)

    var sourceCode: String = ""

    override val parent: SirModule
        get() = module
    
    init {
        module.files.add(this)
    }

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    // TODO Replace SwiftPoet with Sir
    val swiftPoetBuilderModifications = mutableListOf<FileSpec.Builder.() -> Unit>()

    override fun toString(): String = "${this::class.simpleName}: $name"

    companion object {

        fun relativePath(namespace: String, name: String): Path =
            Path.of("$namespace/$namespace.$name.swift")

        const val skieNamespace: String = "Skie"
    }
}
