package co.touchlab.skie.sir.element

sealed interface SirValueParameterParent {

    val valueParameters: MutableList<SirValueParameter>
}

fun SirValueParameterParent.copyValueParametersFrom(other: SirValueParameterParent) {
    val copies = other.valueParameters.map {
        SirValueParameter(
            label = it.label,
            name = it.name,
            type = it.type,
        )
    }
//     WIP("Solve type substitutions")
}
