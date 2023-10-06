package co.touchlab.skie.sir.element

sealed interface SirDeclaration : SirElement {

    val parent: SirDeclarationParent

    var visibility: SirVisibility
}

val SirDeclaration.module: SirModule
    get() = parent.module

val SirDeclaration.firstParentThatIsNotNamespace: SirDeclarationParent
    get() = if (parent is SirDeclarationNamespace) parent.firstParentThatIsNotNamespace else parent

val SirDeclaration.topLevelParent: SirTopLevelDeclarationParent
    get() = (parent as? SirTopLevelDeclarationParent) ?: parent.topLevelParent

@Suppress("RecursivePropertyAccessor")
val SirDeclaration.file: SirFile?
    get() = parent as? SirFile ?: (parent as? SirDeclaration)?.file
