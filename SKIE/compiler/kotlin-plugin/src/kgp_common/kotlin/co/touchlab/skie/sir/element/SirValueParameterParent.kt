package co.touchlab.skie.sir.element

sealed interface SirValueParameterParent : SirDeclaration {

    val valueParameters: MutableList<SirValueParameter>
}

// WIP 2 Use
fun SirValueParameterParent.copyValueParametersFrom(other: SirValueParameterParent) {
    val copies = other.valueParameters.map {
        SirValueParameter(
            label = it.label,
            name = it.name,
            type = it.type,
        )
    }
// WIP 2    WIP("Solve type substitutions")
}
