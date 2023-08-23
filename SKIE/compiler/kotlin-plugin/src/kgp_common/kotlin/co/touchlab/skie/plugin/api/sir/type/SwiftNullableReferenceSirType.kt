package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

data class SwiftNullableReferenceSirType(
    val nonNullType: SwiftNonNullReferenceSirType,
    val isNullableResult: Boolean = false,
) : SwiftReferenceSirType {
    override val declaration: SwiftIrDeclaration
        get() = nonNullType.declaration

    override val directChildren: List<SirType> = listOf(nonNullType)

    override fun toSwiftPoetUsage(): TypeName = nonNullType.toSwiftPoetUsage().makeOptional()

    override fun toString(): String = asString()
}
