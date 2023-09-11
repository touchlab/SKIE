package co.touchlab.skie.plugin.api.sir.element

import co.touchlab.skie.plugin.api.sir.element.util.sirEnumCaseParent
import co.touchlab.skie.plugin.api.sir.type.SirType

class SirEnumCaseAssociatedValue(
    var type: SirType,
    parent: SirEnumCase,
) : SirElement {

    var parent: SirEnumCase by sirEnumCaseParent(parent)

    override fun toString(): String =
        "${this::class.simpleName} of $parent: $type"
}
