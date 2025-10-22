package co.touchlab.skie.sir.element

sealed interface SirTypeParameterParent {

    val typeParameters: MutableList<SirTypeParameter>

    object None : SirTypeParameterParent {

        override val typeParameters: MutableList<SirTypeParameter>
            get() = mutableListOf()

        override fun toString(): String = "${SirTypeParameterParent::class.simpleName}.${this::class.simpleName}"
    }
}

fun SirTypeParameterParent.copyTypeParametersFrom(other: SirTypeParameterParent) {
    copyTypeParametersFrom(other.typeParameters)
}

fun SirTypeParameterParent.copyTypeParametersFrom(
    copiedTypeParameters: List<SirTypeParameter>,
    allTypeParameters: List<SirTypeParameter> = copiedTypeParameters,
) {
    val copiesWithOriginal = copiedTypeParameters.map {
        it to SirTypeParameter(it.name)
    }

    val nonCopiedTypeParameters = allTypeParameters - copiedTypeParameters.toSet()

    val nonCopiedSubstitutions = nonCopiedTypeParameters.map { nonCopiedTypeParameter ->
        val existingTypeParameter = (typeParameters.firstOrNull { it.name == nonCopiedTypeParameter.name }
            ?: error("Type parameter ${nonCopiedTypeParameter.name} not found in parent scope"))

        nonCopiedTypeParameter to existingTypeParameter
    }

    val substitutions = (copiesWithOriginal + nonCopiedSubstitutions).toMap()

    copiesWithOriginal.forEach { (original, copy) ->
        // TODO This is not entirely correct, because we don't substitute type parameters from parent scope as nested scopes are not implemented yet.
        val bounds = original.bounds.map { it.copy(it.type.substituteTypeParameters(substitutions)) }

        copy.bounds.addAll(bounds)
    }
}
