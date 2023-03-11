package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.TypeName

// data class SwiftPointerTypeModel(
//     val pointee: SwiftTypeModel,
//     val nullable: Boolean = false,
// ) : SwiftTypeModel {
//
//     override val stableFqName: SwiftFqName
//         get() = SwiftFqName.External.Swift("UnsafeMutableRawPointer").let {
//             if (nullable) SwiftFqName.Optional(it) else it
//         }
// }

data class SwiftPointerSirType(
    val pointee: SirType,
    val nullable: Boolean = false,
) : SirType {

    override val declaration = BuiltinDeclarations.Swift.UnsafeMutableRawPointer

    override fun toSwiftPoetUsage(): TypeName = if (nullable) {
        declaration.internalName.toSwiftPoetName().makeOptional()
    } else {
        declaration.internalName.toSwiftPoetName()
    }
}
