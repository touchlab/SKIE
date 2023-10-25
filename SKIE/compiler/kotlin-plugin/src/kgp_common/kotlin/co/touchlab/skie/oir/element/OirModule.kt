package co.touchlab.skie.oir.element

// Instantiate only in OirProvider
sealed class OirModule(
    val name: String,
) : OirElement {

    class Kotlin(
        name: String,
    ) : OirModule(name) {

        val files: MutableList<OirFile> = mutableListOf()
    }

    class External(
        name: String,
    ) : OirModule(name), OirTopLevelDeclarationParent {

        override val module: OirModule
            get() = this

        override var declarations: MutableList<OirTopLevelDeclaration> = mutableListOf()
    }

    override fun toString(): String = "OirModule${this::class.simpleName}: $name"
}
