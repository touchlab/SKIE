package co.touchlab.skie.sir.type

import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.AnyTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeName

sealed class SpecialSirType(
    private val typeName: TypeName,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isPrimitive: Boolean = false

    override val directlyReferencedTypes: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName =
        typeName

    object Self : SpecialSirType(SelfTypeName.INSTANCE)

    object Any : SpecialSirType(AnyTypeName.INSTANCE)

    object Protocol : SpecialSirType(DeclaredTypeName.qualifiedLocalTypeName("Protocol"))
}
