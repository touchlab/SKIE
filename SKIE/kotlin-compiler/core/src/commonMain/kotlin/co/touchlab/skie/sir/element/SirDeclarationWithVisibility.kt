package co.touchlab.skie.sir.element

sealed interface SirDeclarationWithVisibility : SirDeclaration {

    var visibility: SirVisibility

    /**
     * If true, the "__" will be added to the name.
     */
    val isReplaced: Boolean

    /**
     * If true, the declaration will be marked as `swift-private`.
     *
     * Affects only Obj-C code.
     */
    var isHidden: Boolean
}
