package co.touchlab.skie.plugin.api.sir.element

sealed interface SirDeclarationParent {

    val module: SirModule
        get() = parent?.module ?: SirModule.None

    val parent: SirDeclarationParent?

    val declarations: MutableList<SirDeclaration>
}

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.rootParent: SirDeclarationParent
    get() = parent?.rootParent ?: this
