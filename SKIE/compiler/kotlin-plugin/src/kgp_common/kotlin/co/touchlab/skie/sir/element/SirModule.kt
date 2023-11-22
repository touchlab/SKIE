package co.touchlab.skie.sir.element

// Instantiate only in SirProvider
sealed class SirModule(
    val name: String,
) : SirElement, SirTopLevelDeclarationParent {

    override val module: SirModule
        get() = this

    override val parent: SirDeclarationParent? = null

    abstract val files: List<SirFile>

    override var declarations: MutableList<SirDeclaration> = mutableListOf()

    class Kotlin(
        name: String,
    ) : SirModule(name) {

        override val files: List<SirFile> = emptyList()
    }

    class Skie(
        name: String,
    ) : SirModule(name) {

        override val files: MutableList<SirFile> = mutableListOf()
    }

    class External(
        name: String,
    ) : SirModule(name) {

        override val files: List<SirFile> = emptyList()
    }

    object Unknown : SirModule("<Unknown>") {

        override val files: List<SirFile> = emptyList()
    }

    object None : SirModule("<None>") {

        override val files: List<SirFile> = emptyList()
    }

    override fun toString(): String = "SirModule${this::class.simpleName}: $name"
}
