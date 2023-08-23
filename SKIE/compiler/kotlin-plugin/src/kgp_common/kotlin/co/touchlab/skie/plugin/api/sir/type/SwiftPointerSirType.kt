package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.TypeName

data class SwiftPointerSirType(
    val pointee: SirType,
    val nullable: Boolean = false,
) : SirType {

    override val declaration = BuiltinDeclarations.Swift.UnsafeMutableRawPointer

    override val directChildren: List<SirType> = listOf(pointee)

    override fun toSwiftPoetUsage(): TypeName = if (nullable) {
        declaration.internalName.toSwiftPoetName().makeOptional()
    } else {
        declaration.internalName.toSwiftPoetName()
    }

    override fun toString(): String = asString()
}
