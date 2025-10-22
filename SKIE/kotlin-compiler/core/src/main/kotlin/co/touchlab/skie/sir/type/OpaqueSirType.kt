package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.map

data class OpaqueSirType(
    val type: SirType,
) : NonNullSirType() {
    override val isHashable: Boolean
        get() = type.isHashable

    override val isReference: Boolean
        get() = type.isReference

    override fun evaluate(): EvaluatedSirType {
        val evaluatedType = lazy { type.evaluate() }

        return EvaluatedSirType.Lazy(
            typeProvider = evaluatedType.map { copy(type = it.type) },
            canonicalNameProvider = evaluatedType.map { "some " + it.canonicalName },
            swiftPoetTypeNameProvider = evaluatedType.map { it.swiftPoetTypeName.makeOpaque() },
            lowestVisibility = evaluatedType.map { it.visibilityConstraint },
            referencedTypeDeclarationsProvider = evaluatedType.map { it.referencedTypeDeclarations },
        )
    }

    override fun inlineTypeAliases(): OpaqueSirType =
        copy(type = type.inlineTypeAliases())

    override fun asHashableType(): OpaqueSirType? =
        type.asHashableType()?.let(::OpaqueSirType)

    override fun asReferenceType(): OpaqueSirType? =
        type.asReferenceType()?.let(::OpaqueSirType)

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): OpaqueSirType =
        copy(type = type.substituteTypeParameters(substitutions))

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): OpaqueSirType =
        copy(type = type.substituteTypeArguments(substitutions))
}
