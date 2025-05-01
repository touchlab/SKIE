package co.touchlab.skie.oir.element

import co.touchlab.skie.sir.element.SirVisibility

enum class OirVisibility {
    Public,
    Internal,
    Private,
}

fun SirVisibility.toOirVisibility(): OirVisibility = when (this) {
    SirVisibility.Public -> OirVisibility.Public
    SirVisibility.Internal -> OirVisibility.Internal
    SirVisibility.Private, SirVisibility.Removed -> OirVisibility.Private
}
