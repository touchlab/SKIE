package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter

data class NullableSirType(
    val type: SirType,
) : SirType() {

    override val isHashable: Boolean
        get() = type.isHashable

    override val isReference: Boolean = false

    override fun evaluate(): EvaluatedSirType<NullableSirType> {
        val evaluatedType = type.evaluate()

        return EvaluatedSirType(
            type = copy(type = evaluatedType.type),
            isValid = evaluatedType.isValid,
            canonicalName = evaluatedType.canonicalName + "?",
            swiftPoetTypeName = evaluatedType.swiftPoetTypeName.makeOptional(),
        )
    }

    override fun inlineTypeAliases(): SirType =
        copy(type = type.inlineTypeAliases())

    override fun asHashableType(): SirType? =
        type.asHashableType()?.let(::NullableSirType)

    override fun asReferenceType(): SirType? =
        null

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): NullableSirType =
        copy(type = type.substituteTypeParameters(substitutions))

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): NullableSirType =
        copy(type = type.substituteTypeArguments(substitutions))
}
