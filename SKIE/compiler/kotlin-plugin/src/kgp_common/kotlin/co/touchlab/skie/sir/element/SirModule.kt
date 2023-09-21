package co.touchlab.skie.sir.element

sealed class SirModule(
    val name: String,
) : SirElement, SirTopLevelDeclarationParent {

    val isMutable: Boolean
        get() = this is Kotlin || this is Skie

    override val module: SirModule
        get() = this

    override val parent: SirDeclarationParent? = null

    override var declarations: MutableList<SirDeclaration> = mutableListOf()

    class Kotlin(
        name: String,
    ) : SirModule(name)

    class KotlinBuiltins(
        name: String,
    ) : SirModule(name)

    class Skie(
        name: String,
    ) : SirModule(name) {

        val files: MutableList<SirFile> = mutableListOf()
    }

    class External(
        name: String,
    ) : SirModule(name)

    object None : SirModule("__None")

    override fun toString(): String = "SirModule${this::class.simpleName}: $name"
}
