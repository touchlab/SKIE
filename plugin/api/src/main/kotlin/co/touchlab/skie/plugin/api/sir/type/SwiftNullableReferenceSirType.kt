package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

data class SwiftNullableReferenceSirType(
    val nonNullType: SwiftNonNullReferenceSirType,
    val isNullableResult: Boolean = false,
) : SwiftReferenceSirType {
    override val declaration: SwiftIrDeclaration
        get() = nonNullType.declaration


    override fun toSwiftPoetUsage(): TypeName = nonNullType.toSwiftPoetUsage().makeOptional()
}
