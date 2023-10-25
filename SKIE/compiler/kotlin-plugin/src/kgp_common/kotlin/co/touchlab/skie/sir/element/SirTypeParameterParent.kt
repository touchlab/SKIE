package co.touchlab.skie.sir.element

sealed interface SirTypeParameterParent {

    val typeParameters: MutableList<SirTypeParameter>

    object None : SirTypeParameterParent {

        override val typeParameters: MutableList<SirTypeParameter>
            get() = mutableListOf()

        override fun toString(): String = "${SirTypeParameterParent::class.simpleName}.${this::class.simpleName}"
    }
}

fun SirTypeParameterParent.copyTypeParametersFrom(other: SirClass) {
    val copiesWithOriginal = other.typeParameters.map {
        it to SirTypeParameter(it.name)
    }

    val substitutions = copiesWithOriginal.toMap()

    copiesWithOriginal.forEach { (original, copy) ->
        // TODO This is not entirely correct, because we don't substitute type parameters from parent scope as nested scopes are not implemented yet.
        val bounds = original.bounds.map { it.substituteTypeParameters(substitutions) }

        copy.bounds.addAll(bounds)
    }
}
