package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.util.map
import io.outfoxx.swiftpoet.TypeVariableName

data class TypeParameterUsageSirType(
    val typeParameter: SirTypeParameter,
    val parentScope: TypeParameterUsageSirType? = null,
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = typeParameter.bounds.any { it.isHashable }

    override val isReference: Boolean
        get() = typeParameter.bounds.any { it.isReference }

    override fun evaluate(): EvaluatedSirType {
        val evaluatedParentScope = lazy { parentScope?.evaluate() }
        val evaluatedTypeParameterBounds = lazy { typeParameter.bounds.map { it.evaluate() } }

        return EvaluatedSirType.Lazy(
            typeProvider = evaluatedParentScope.map { copy(parentScope = it?.type as TypeParameterUsageSirType?) },
            canonicalNameProvider = evaluatedTypeParameterBounds.map { bounds -> "[${typeParameter.name}: ${bounds.joinToString { it.canonicalName }}]" },
            swiftPoetTypeNameProvider = evaluatedParentScope.map {
                it?.let { TypeVariableName(it.swiftPoetTypeName.name + "." + typeParameter.name) } ?: TypeVariableName(typeParameter.name)
            },
            lowestVisibility = lazy { SirVisibility.Public },
            referencedTypeDeclarationsProvider = lazy { emptySet() },
        )
    }

    override fun inlineTypeAliases(): SirType =
        this

    override fun asHashableType(): SirType? =
        if (typeParameter.bounds.any { it.asHashableType() != null }) {
            this
        } else {
            null
        }

    override fun asReferenceType(): SirType? =
        if (typeParameter.bounds.any { it.asReferenceType() != null }) {
            this
        } else {
            null
        }

    fun typeParameter(typeParameter: SirTypeParameter): TypeParameterUsageSirType =
        TypeParameterUsageSirType(typeParameter, this)

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): TypeParameterUsageSirType {
        val parentScope = parentScope?.substituteTypeParameters(substitutions)

        return copy(
            typeParameter = substitutions[typeParameter] ?: typeParameter,
            parentScope = parentScope,
        )
    }

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SirType =
        substitutions[typeParameter] ?: this
}
