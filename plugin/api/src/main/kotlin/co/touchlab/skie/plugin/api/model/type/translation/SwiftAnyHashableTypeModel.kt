package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations

// object SwiftAnyHashableTypeModel : SwiftNonNullReferenceTypeModel {
//
//     override val stableFqName: SwiftFqName.External
//         get() = SwiftFqName.External.Swift.AnyHashable
// }

object SwiftAnyHashableSirType: SwiftNonNullReferenceSirType {
    override val declaration = BuiltinDeclarations.Swift.AnyHashable
    override fun toSwiftPoetUsage() = declaration.internalName.toSwiftPoetName()
}
