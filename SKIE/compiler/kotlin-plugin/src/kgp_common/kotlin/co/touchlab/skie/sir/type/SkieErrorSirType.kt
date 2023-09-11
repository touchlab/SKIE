package co.touchlab.skie.sir.type

import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

sealed class SkieErrorSirType(
    private val name: String,
) : NonNullSirType() {

    override val isHashable: Boolean = true

    override val isPrimitive: Boolean = false

    override val directlyReferencedTypes: List<SirType> = emptyList()

    override fun toSwiftPoetUsage() =
        DeclaredTypeName.qualifiedLocalTypeName(name)

    object Lambda : SkieErrorSirType("__SkieLambdaErrorType")
}
