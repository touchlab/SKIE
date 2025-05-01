package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.map

data class NullableSirType(val type: SirType) : SirType() {

    override val isHashable: Boolean
        get() = type.isHashable

    override val isReference: Boolean = false

    override fun evaluate(): EvaluatedSirType {
        val evaluatedType = lazy { type.evaluate() }

        return EvaluatedSirType.Lazy(
            typeProvider = evaluatedType.map { copy(type = it.type) },
            canonicalNameProvider = evaluatedType.map { it.canonicalName + "?" },
            swiftPoetTypeNameProvider = evaluatedType.map { it.swiftPoetTypeName.makeOptional() },
            lowestVisibility = evaluatedType.map { it.visibilityConstraint },
            referencedTypeDeclarationsProvider = evaluatedType.map { it.referencedTypeDeclarations },
        )
    }

    override fun inlineTypeAliases(): SirType = copy(type = type.inlineTypeAliases())

    override fun asHashableType(): SirType? = type.asHashableType()?.let(::NullableSirType)

    override fun asReferenceType(): SirType? = null

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): NullableSirType =
        copy(type = type.substituteTypeParameters(substitutions))

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): NullableSirType =
        copy(type = type.substituteTypeArguments(substitutions))
}
