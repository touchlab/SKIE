package co.touchlab.skie.util

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.sir.element.SirVisibility

fun SkieVisibility.Level.toSirVisibility(isWrapped: Boolean = false): SirVisibility =
    when (this) {
        SkieVisibility.Level.Public -> SirVisibility.Public
        SkieVisibility.Level.PublicButHidden -> SirVisibility.Public
        SkieVisibility.Level.PublicButReplaced -> SirVisibility.Public
        SkieVisibility.Level.Internal -> SirVisibility.Internal
        SkieVisibility.Level.InternalAndReplaced -> SirVisibility.Internal
        SkieVisibility.Level.InternalIfWrapped -> if (isWrapped) SirVisibility.Internal else SirVisibility.Public
        SkieVisibility.Level.Private -> SirVisibility.Removed
    }
