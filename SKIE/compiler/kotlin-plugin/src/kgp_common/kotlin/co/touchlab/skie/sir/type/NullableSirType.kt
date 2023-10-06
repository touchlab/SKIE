package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

data class NullableSirType(
    val type: SirType,
) : SirType() {

    override val isHashable: Boolean
        get() = type.isHashable

    override val isPrimitive: Boolean
        get() = false

    override val canonicalName: String
        get() = type.canonicalName + "?"

    override val directlyReferencedTypes: List<SirType> = listOf(type)

    override fun toNonNull(): NonNullSirType =
        type.toNonNull()

    override fun toSwiftPoetTypeName(): TypeName =
        type.toSwiftPoetTypeName().makeOptional()
}
