package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.map

data class ExistentialSirType(val type: SirType) : NonNullSirType() {
    override val isHashable: Boolean
        get() = type.isHashable

    override val isReference: Boolean
        get() = type.isReference

    override fun evaluate(): EvaluatedSirType {
        val evaluatedType = lazy { type.evaluate() }

        return EvaluatedSirType.Lazy(
            typeProvider = evaluatedType.map { copy(type = it.type) },
            canonicalNameProvider = evaluatedType.map { "any " + it.canonicalName },
            swiftPoetTypeNameProvider = evaluatedType.map { it.swiftPoetTypeName.makeExistential() },
            lowestVisibility = evaluatedType.map { it.visibilityConstraint },
            referencedTypeDeclarationsProvider = evaluatedType.map { it.referencedTypeDeclarations },
        )
    }

    override fun inlineTypeAliases(): ExistentialSirType = copy(type = type.inlineTypeAliases())

    override fun asHashableType(): ExistentialSirType? = type.asHashableType()?.let(::ExistentialSirType)

    override fun asReferenceType(): ExistentialSirType? = type.asReferenceType()?.let(::ExistentialSirType)

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): ExistentialSirType =
        copy(type = type.substituteTypeParameters(substitutions))

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): ExistentialSirType =
        copy(type = type.substituteTypeArguments(substitutions))
}
