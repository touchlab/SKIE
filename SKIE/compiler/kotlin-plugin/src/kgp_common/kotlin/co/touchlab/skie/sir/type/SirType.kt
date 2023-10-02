package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

sealed class SirType {

    abstract val isHashable: Boolean

    abstract val isPrimitive: Boolean

    open val directlyReferencedTypes: List<SirType> = emptyList()

    fun allReferencedTypes(): List<SirType> =
        listOf(this) + directlyReferencedTypes.flatMap { it.allReferencedTypes() }

    override fun toString(): String =
        toSwiftPoetTypeName().toString()

    abstract fun toSwiftPoetTypeName(): TypeName

    abstract fun toNonNull(): NonNullSirType
}

fun SirType.optional(): NullableSirType =
    NullableSirType(this)
