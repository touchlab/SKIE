package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import io.outfoxx.swiftpoet.TypeVariableName

data class TypeParameterUsageSirType(
    val typeParameter: SirTypeParameter,
    val parentScope: TypeParameterUsageSirType? = null,
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = typeParameter.bounds.any { it.isHashable }

    override val isReference: Boolean
        get() = typeParameter.bounds.any { it.isReference }

    override fun evaluate(): EvaluatedSirType<TypeParameterUsageSirType> {
        val evaluatedParentScope = parentScope?.evaluate()
        val evaluatedTypeParameterBounds = typeParameter.bounds.map { it.evaluate() }

        return EvaluatedSirType(
            type = copy(parentScope = evaluatedParentScope?.type),
            isValid = evaluatedTypeParameterBounds.all { it.isValid } && (evaluatedParentScope?.isValid ?: true),
            canonicalName = "[${typeParameter.name}: ${evaluatedTypeParameterBounds.joinToString { it.canonicalName }}]",
            swiftPoetTypeName = evaluatedParentScope?.let { TypeVariableName(it.swiftPoetTypeName.name + "." + typeParameter.name) }
                ?: TypeVariableName(typeParameter.name),
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
