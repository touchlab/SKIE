package co.touchlab.skie.sir.element

sealed interface SirDeclarationParent {

    val module: SirModule
        get() = parent?.module ?: SirModule.None

    val parent: SirDeclarationParent?

    val declarations: MutableList<SirDeclaration>
}

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.firstParentThatIsNotNamespace: SirDeclarationParent
    get() = if (this is SirDeclarationNamespace) (parent as? SirDeclarationNamespace)?.firstParentThatIsNotNamespace ?: parent ?: this else this
