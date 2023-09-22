package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirEnumCaseParent
import co.touchlab.skie.sir.type.SirType

class SirEnumCaseAssociatedValue(
    var type: SirType,
    parent: SirEnumCase,
) : SirElement {

    var parent: SirEnumCase by sirEnumCaseParent(parent)

    override fun toString(): String =
        "${this::class.simpleName} of $parent: $type"
}