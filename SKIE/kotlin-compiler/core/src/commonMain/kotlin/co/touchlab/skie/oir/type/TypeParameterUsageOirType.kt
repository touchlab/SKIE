package co.touchlab.skie.oir.type

import co.touchlab.skie.oir.element.OirTypeParameter

data class TypeParameterUsageOirType(val typeParameter: OirTypeParameter) : NonNullReferenceOirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        typeParameter.name.withAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
}
