package co.touchlab.skie.oir.element

sealed interface OirCallableDeclarationParent : OirElement {

    val callableDeclarations: MutableList<OirCallableDeclaration>
}
