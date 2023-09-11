package co.touchlab.skie.plugin.api.sir.element

sealed interface SirDeclaration : SirElement {

    val parent: SirDeclarationParent
}

val SirDeclaration.module: SirModule
    get() = parent.module

val SirDeclaration.rootParent: SirDeclarationParent
    get() = parent.rootParent

val SirDeclaration.file: SirFile?
    get() = parent as? SirFile ?: (parent as? SirDeclaration)?.file
