package co.touchlab.skie.sir.element

enum class SirVisibility {

    Public,
    Internal,
    Private,

    /** Applicable only to generated code, existing code cannot be removed and will be marked as private instead. */
    Removed,
}

fun SirVisibility.coerceAtMost(limit: SirVisibility): SirVisibility =
    when (limit) {
        SirVisibility.Public -> this
        SirVisibility.Internal -> if (this == SirVisibility.Public) SirVisibility.Internal else this
        SirVisibility.Private -> if (this == SirVisibility.Removed) SirVisibility.Removed else SirVisibility.Private
        SirVisibility.Removed -> SirVisibility.Removed
    }

fun List<SirVisibility>.minimumVisibility(): SirVisibility =
    this.fold(SirVisibility.Public) { acc, visibility -> acc.coerceAtMost(visibility) }

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
