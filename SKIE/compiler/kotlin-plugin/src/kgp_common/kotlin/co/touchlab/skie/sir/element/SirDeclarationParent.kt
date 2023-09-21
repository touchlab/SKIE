package co.touchlab.skie.sir.element

sealed interface SirDeclarationParent {

    val module: SirModule
        get() = parent?.module ?: SirModule.None

    val parent: SirDeclarationParent?

    val declarations: MutableList<SirDeclaration>
}

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.namespaceParent: SirDeclarationParent
    get() = if (this is SirDeclarationNamespace) (parent as? SirDeclarationNamespace)?.namespaceParent ?: parent ?: this else this
