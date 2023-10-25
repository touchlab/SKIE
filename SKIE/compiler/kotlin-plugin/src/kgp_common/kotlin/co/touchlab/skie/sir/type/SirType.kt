package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter

sealed class SirType {

    abstract val isHashable: Boolean

    abstract val isReference: Boolean

    abstract fun evaluate(): EvaluatedSirType<SirType>

    open fun asHashableType(): SirType? = null

    open fun asReferenceType(): SirType? = null

    abstract fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SirType

    abstract fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SirType

    override fun toString(): String =
        evaluate().swiftPoetTypeName.toString()
}

fun SirType.toNullable(condition: Boolean = true): SirType =
    if (condition) {
        NullableSirType(this)
    } else {
        this
    }

