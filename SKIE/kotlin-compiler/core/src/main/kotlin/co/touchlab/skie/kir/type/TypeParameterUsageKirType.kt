package co.touchlab.skie.kir.type

import co.touchlab.skie.kir.element.KirTypeParameter

data class TypeParameterUsageKirType(
    val typeParameter: KirTypeParameter,
) : NonNullReferenceKirType()
