package co.touchlab.skie.plugin.api.sir.type

import io.outfoxx.swiftpoet.TypeName

sealed class SirType {

    abstract val isHashable: Boolean

    abstract val isPrimitive: Boolean

    open val directlyReferencedTypes: List<SirType> = emptyList()

    fun allReferencedTypes(): List<SirType> =
        listOf(this) + directlyReferencedTypes.flatMap { it.allReferencedTypes() }

    override fun toString(): String =
        toSwiftPoetUsage().toString()

    abstract fun toSwiftPoetUsage(): TypeName

    abstract fun toNonNull(): NonNullSirType
}
