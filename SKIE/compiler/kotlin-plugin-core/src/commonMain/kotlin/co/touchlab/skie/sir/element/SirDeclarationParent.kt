package co.touchlab.skie.sir.element

sealed interface SirDeclarationParent {

    val module: SirModule
        get() = parent?.module ?: SirModule.None

    val parent: SirDeclarationParent?

    val declarations: MutableList<SirDeclaration>

    object None : SirTopLevelDeclarationParent {

        override val parent: SirDeclarationParent? = null

        override val declarations: MutableList<SirDeclaration>
            get() = mutableListOf()

        override fun toString(): String = "${SirDeclarationParent::class.simpleName}.${this::class.simpleName}"
    }
}

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.firstParentThatIsNotNamespace: SirDeclarationParent
    get() = if (this is SirDeclarationNamespace) (parent as? SirDeclarationNamespace)?.firstParentThatIsNotNamespace ?: parent ?: this else this

@Suppress("RecursivePropertyAccessor")
val SirDeclarationParent.topLevelParent: SirTopLevelDeclarationParent?
    get() = (parent as? SirTopLevelDeclarationParent) ?: parent?.topLevelParent

fun SirDeclarationParent.getAllDeclarationsRecursively(): List<SirDeclaration> {
    val declarationParents = declarations.filterIsInstance<SirDeclarationParent>()

    return declarations + declarationParents.flatMap { it.getAllDeclarationsRecursively() }
}

fun SirDeclarationParent.coerceScope(scope: SirScope): SirScope =
    if (this !is SirDeclarationNamespace) SirScope.Global else scope
