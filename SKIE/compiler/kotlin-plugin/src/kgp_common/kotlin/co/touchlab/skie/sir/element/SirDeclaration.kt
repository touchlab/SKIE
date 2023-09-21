package co.touchlab.skie.sir.element

sealed interface SirDeclaration : SirElement {

    val parent: SirDeclarationParent
}

val SirDeclaration.module: SirModule
    get() = parent.module

val SirDeclaration.namespaceParent: SirDeclarationParent
    get() = if (parent is SirDeclarationNamespace) parent.namespaceParent else parent

val SirDeclaration.file: SirFile?
    get() = parent as? SirFile ?: (parent as? SirDeclaration)?.file
