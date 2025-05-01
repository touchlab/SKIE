package co.touchlab.skie.sir.element

// Instantiate only in SirProvider
sealed class SirModule(val name: String) : SirElement {

    open val builtInFile by lazy {
        SirBuiltInFile(this)
    }

    open val files: List<SirFile> by lazy {
        listOf(builtInFile)
    }

    class Kotlin(name: String) : SirModule(name)

    class Skie(name: String) : SirModule(name) {

        override val files: MutableList<SirFile> by lazy {
            mutableListOf(builtInFile)
        }
    }

    class External(name: String) : SirModule(name)

    class Unknown : SirModule("<Unknown>")

    object None : SirModule("<None>") {

        override val files: List<SirFile> = emptyList()

        override val builtInFile: SirBuiltInFile by lazy<SirBuiltInFile> {
            error("None module does not have a built-in file.")
        }
    }

    override fun toString(): String = "SirModule${this::class.simpleName}: $name"
}

fun SirModule.getAllDeclarationsRecursively(): List<SirDeclaration> =
    files.filterIsInstance<SirDeclarationParent>().flatMap { it.getAllDeclarationsRecursively() }
