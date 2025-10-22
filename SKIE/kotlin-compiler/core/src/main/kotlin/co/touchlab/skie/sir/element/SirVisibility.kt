package co.touchlab.skie.sir.element

enum class SirVisibility : Comparable<SirVisibility> {

    /** The code will not be generated, or in the case of Obj-C included in the header file. */
    Removed,

    /** Obj-C will be handled the same as if it was Removed. */
    Private,
    Internal,
    Public;
}

fun List<SirVisibility>.minimumVisibility(): SirVisibility =
    this.fold(SirVisibility.Public) { acc, visibility -> acc.coerceAtMost(visibility) }

fun List<SirVisibility>.maximumVisibility(): SirVisibility =
    this.fold(SirVisibility.Removed) { acc, visibility -> acc.coerceAtLeast(visibility) }

val SirVisibility.isAccessibleFromOtherModules: Boolean
    get() = this == SirVisibility.Public

val SirDeclarationWithVisibility.isExported: Boolean
    get() = visibility.isAccessibleFromOtherModules

val SirVisibility.isAccessible: Boolean
    get() = when (this) {
        SirVisibility.Private, SirVisibility.Removed -> false
        else -> true
    }

val SirDeclarationWithVisibility.isAccessible: Boolean
    get() = visibility.isAccessible

val SirVisibility.isRemoved: Boolean
    get() = this == SirVisibility.Removed

val SirDeclarationWithVisibility.isRemoved: Boolean
    get() = visibility.isRemoved || parent == SirDeclarationParent.None
