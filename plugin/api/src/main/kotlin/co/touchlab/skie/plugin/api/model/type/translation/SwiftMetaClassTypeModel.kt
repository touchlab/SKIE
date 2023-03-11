package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

// object SwiftMetaClassTypeModel : SwiftNonNullReferenceTypeModel {
//
//     override val stableFqName: SwiftFqName
//         get() = SwiftFqName.External.Swift("AnyClass")
// }

object SwiftMetaClassSirType: SwiftNonNullReferenceSirType {

    override val declaration = BuiltinDeclarations.AnyClass

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedTypeName("Swift.AnyClass")
}
