package co.touchlab.skie.sir.element

enum class SirVisibility {

    Public,
    PublicButHidden,
    PublicButReplaced,
    Internal,
    Private,
    Removed,
}

val SirVisibility.isAccessibleFromOtherModules
    get() = when (this) {
        SirVisibility.Public,
        SirVisibility.PublicButHidden,
        SirVisibility.PublicButReplaced,
        -> true
        else -> false
    }
