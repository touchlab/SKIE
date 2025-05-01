package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirEnumCaseAssociatedValueParent
import co.touchlab.skie.sir.type.SirType

class SirEnumCaseAssociatedValue(var type: SirType, parent: SirEnumCase) : SirElement {

    var parent: SirEnumCase by sirEnumCaseAssociatedValueParent(parent)

    override fun toString(): String = "${this::class.simpleName} of $parent: $type"

    companion object {

        context(SirEnumCase)
        operator fun invoke(type: SirType): SirEnumCaseAssociatedValue = SirEnumCaseAssociatedValue(
            type = type,
            parent = this@SirEnumCase,
        )
    }
}
