package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirConditionalConstraintParent
import co.touchlab.skie.sir.type.SirType

class SirConditionalConstraint(
    val typeParameter: SirTypeParameter,
    parent: SirExtension,
    bounds: List<SirType> = emptyList(),
) : SirElement {

    val bounds: MutableList<SirType> = bounds.toMutableList()

    var parent: SirExtension by sirConditionalConstraintParent(parent)

    override fun toString(): String = "${this::class.simpleName}: $typeParameter : ${bounds.joinToString("&")}"

    companion object {

        context(SirExtension)
        operator fun invoke(
            typeParameter: SirTypeParameter,
            bounds: List<SirType> = emptyList(),
        ): SirConditionalConstraint =
            SirConditionalConstraint(
                typeParameter = typeParameter,
                parent = this@SirExtension,
                bounds = bounds,
            )
    }
}
