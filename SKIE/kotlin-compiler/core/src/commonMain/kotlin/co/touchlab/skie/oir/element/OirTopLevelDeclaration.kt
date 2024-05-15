package co.touchlab.skie.oir.element

sealed interface OirTopLevelDeclaration {

    val parent: OirTopLevelDeclarationParent

    val module: OirModule
        get() = parent.module
}
