package co.touchlab.skie.sir.element

// Instantiate only in SirModule
class SirBuiltInFile(override val module: SirModule) :
    SirFile,
    SirTopLevelDeclarationParent {

    override val parent: SirDeclarationParent?
        get() = null

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: <built-in>(${module.name})"
}
