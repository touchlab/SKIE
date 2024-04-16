package co.touchlab.skie.oir.element

sealed interface OirTopLevelDeclarationParent : OirElement {

    val declarations: MutableList<OirTopLevelDeclaration>

    val module: OirModule
}
