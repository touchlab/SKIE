package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

sealed class SkieErrorSirType(
    private val canonicalName: String,
) : NonNullSirType() {

    override val isHashable: Boolean = true

    override val isReference: Boolean = true

    override fun evaluate(): EvaluatedSirType<SkieErrorSirType> =
        EvaluatedSirType(
            type = this,
            isValid = false,
            canonicalName = canonicalName,
            swiftPoetTypeName = DeclaredTypeName.qualifiedLocalTypeName(canonicalName),
        )

    // To ensure this type is never erased
    override fun asHashableType(): SirType? =
        this

    override fun asReferenceType(): SirType? =
        this

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SkieErrorSirType =
        this

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SkieErrorSirType =
        this

    object Lambda : SkieErrorSirType("__SkieLambdaErrorType")
}
