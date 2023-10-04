package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.KotlinErrorSirType
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType

sealed interface SirTypeParameterParent {

    val typeParameters: MutableList<SirTypeParameter>
}

fun SirTypeParameterParent.copyTypeParametersFrom(other: SirClass) {
    val copiesWithOriginal = other.typeParameters.map {
        it to SirTypeParameter(it.name)
    }

    val substitutions = copiesWithOriginal.toMap()

    copiesWithOriginal.forEach { (original, copy) ->
        val bounds = original.bounds.map { it.substituteTypeParameters(substitutions) }

        copy.bounds.addAll(bounds)
    }
}

// WIP 2 To SirType
private fun SirType.substituteTypeParameters(
    substitutions: Map<SirTypeParameter, SirTypeParameter>,
): SirType =
    when (this) {
        is TypeParameterUsageSirType -> substitutions[typeParameter]?.toTypeParameterUsage() ?: this
        is DeclaredSirType -> DeclaredSirType(
            declaration = declaration,
            typeArguments = typeArguments.map { it.substituteTypeParameters(substitutions) },
        )
        is LambdaSirType -> LambdaSirType(
            returnType = returnType.substituteTypeParameters(substitutions),
            valueParameterTypes = valueParameterTypes.map { it.substituteTypeParameters(substitutions) },
            isEscaping = isEscaping,
        )
        is NullableSirType -> NullableSirType(type.substituteTypeParameters(substitutions))
        is SkieErrorSirType,
        is SpecialSirType,
        KotlinErrorSirType,
        -> this
    }
