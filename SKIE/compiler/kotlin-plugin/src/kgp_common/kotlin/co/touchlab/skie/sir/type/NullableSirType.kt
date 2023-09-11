package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

data class NullableSirType(
    val type: SirType,
) : SirType() {

    override val isHashable: Boolean
        get() = type.isHashable

    override val isPrimitive: Boolean
        get() = false

    override val directlyReferencedTypes: List<SirType> = listOf(type)

    override fun toNonNull(): NonNullSirType =
        type.toNonNull()

    override fun toSwiftPoetUsage(): TypeName =
        type.toSwiftPoetUsage().makeOptional()
}
