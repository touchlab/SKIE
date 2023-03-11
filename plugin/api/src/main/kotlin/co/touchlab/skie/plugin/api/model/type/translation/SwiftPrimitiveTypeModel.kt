package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import io.outfoxx.swiftpoet.TypeName

// sealed class SwiftPrimitiveTypeModel(
//     override val stableFqName: SwiftFqName,
// ) : SwiftTypeModel {
//

//
// }

// TODO: Doesn't need to be its own type
sealed class SwiftPrimitiveSirType(
    override val declaration: SwiftIrTypeDeclaration.External,
) : SirType {

    object NSUInteger : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.UInt)
    object Bool : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Bool)
    @Suppress("ClassName")
    object unichar : SwiftPrimitiveSirType(BuiltinDeclarations.Foundation.unichar)
    object Int8 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Int8)
    object Int16 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Int16)
    object Int32 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Int32)
    object Int64 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Int64)
    object UInt8 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.UInt8)
    object UInt16 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.UInt16)
    object UInt32 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.UInt32)
    object UInt64 : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.UInt64)
    object Float : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Float)
    object Double : SwiftPrimitiveSirType(BuiltinDeclarations.Swift.Double)

    override fun toSwiftPoetUsage(): TypeName = declaration.internalName.toSwiftPoetName()
}
