package co.touchlab.skie.sir.element

enum class SirVisibility {

    Public,
    PublicButHidden,
    PublicButReplaced,
    Internal,
    Private,
    Removed,
}

fun SirVisibility.toSwiftVisibility(): SirVisibility = when (this) {
    SirVisibility.Public,
    SirVisibility.PublicButHidden,
    SirVisibility.PublicButReplaced,
    -> SirVisibility.Public
    SirVisibility.Internal -> SirVisibility.Internal
    SirVisibility.Private -> SirVisibility.Private
    SirVisibility.Removed -> SirVisibility.Removed
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
