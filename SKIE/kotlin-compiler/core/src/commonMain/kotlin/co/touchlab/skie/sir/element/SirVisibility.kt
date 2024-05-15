package co.touchlab.skie.sir.element

enum class SirVisibility {

    Public,
    /** Applicable only to Obj-C code, Swift code will use Public instead. */
    PublicButHidden,
    Internal,
    Private,
    /** Applicable only to generated code, existing code cannot be removed and will be marked as private instead. */
    Removed,
}

val SirVisibility.isAccessibleFromOtherModules: Boolean
    get() = when (toSwiftVisibility()) {
        SirVisibility.Public -> true
        else -> false
    }

val SirDeclaration.isExported: Boolean
    get() = visibility.isAccessibleFromOtherModules

val SirVisibility.isAccessible: Boolean
    get() = when (toSwiftVisibility()) {
        SirVisibility.Private, SirVisibility.Removed -> false
        else -> true
    }

val SirDeclaration.isAccessible: Boolean
    get() = visibility.isAccessible

val SirVisibility.isRemoved: Boolean
    get() = when (this) {
        SirVisibility.Removed -> true
        else -> false
    }

val SirDeclaration.isRemoved: Boolean
    get() = visibility.isRemoved || parent == SirDeclarationParent.None

private fun SirVisibility.toSwiftVisibility(): SirVisibility = when (this) {
    SirVisibility.Public,
    SirVisibility.PublicButHidden,
    -> SirVisibility.Public
    SirVisibility.Internal -> SirVisibility.Internal
    SirVisibility.Private -> SirVisibility.Private
    SirVisibility.Removed -> SirVisibility.Removed
}
