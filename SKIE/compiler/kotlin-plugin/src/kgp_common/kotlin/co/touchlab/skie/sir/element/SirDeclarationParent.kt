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

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.topLevelParent: SirTopLevelDeclarationParent
    get() = (parent as? SirTopLevelDeclarationParent) ?: parent?.topLevelParent ?: module

fun SirDeclarationParent.getAllDeclarationsRecursively(): List<SirDeclaration> =
    declarations + declarations.filterIsInstance<SirDeclarationParent>().flatMap { it.getAllDeclarationsRecursively() } +
    if (this is SirModule.Skie) {
        files.flatMap { it.getAllDeclarationsRecursively() }
    } else {
        emptyList()
    }

