package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirConditionalConstraintParent

class SirConditionalConstraint(
    val typeParameter: SirTypeParameter,
    parent: SirConditionalConstraintParent,
    bounds: List<SirTypeParameter.Bound> = emptyList(),
) : SirElement {

    val bounds: MutableList<SirTypeParameter.Bound> = bounds.toMutableList()

    var parent: SirConditionalConstraintParent by sirConditionalConstraintParent(parent)

    override fun toString(): String = "${this::class.simpleName}: $typeParameter : ${bounds.joinToString("&")}"

    companion object {

        context(SirConditionalConstraintParent)
        operator fun invoke(
            typeParameter: SirTypeParameter,
            bounds: List<SirTypeParameter.Bound> = emptyList(),
        ): SirConditionalConstraint = SirConditionalConstraint(
            typeParameter = typeParameter,
            parent = this@SirConditionalConstraintParent,
            bounds = bounds,
        )
    }
}
