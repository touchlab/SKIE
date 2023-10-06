package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

object KotlinErrorSirType : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isPrimitive: Boolean = false

    override val canonicalName: String = "KOTLIN_ERROR"

    override val directlyReferencedTypes: List<SirType> = emptyList()

    override fun toSwiftPoetTypeName(): TypeName {
        throw UnsupportedOperationException("Error type cannot be used in SwiftPoet.")
    }

    override fun toString(): String = this::class.simpleName!!
}
