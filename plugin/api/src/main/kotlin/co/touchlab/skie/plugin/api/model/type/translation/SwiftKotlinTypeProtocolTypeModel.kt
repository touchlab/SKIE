package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

// data class SwiftKotlinTypeProtocolTypeModel(
//     val model: KotlinTypeSwiftModel,
// ) : SwiftNonNullReferenceTypeModel {
//
//     override val bridgedOrStableFqName: SwiftFqName
//         get() = model.bridgedOrStableFqName
//     override val isSwiftSymbol: Boolean
//         get() = model.isSwiftSymbol
//
//     override fun fqName(separator: String): SwiftFqName = model.fqName(separator)
//
//     override val stableFqName: SwiftFqName.NominalType
//         get() = model.stableFqName
//     override val containingType: KotlinClassSwiftModel?
//         get() = model.containingType
//     override val identifier: String
//         get() = model.identifier
//     override val swiftGenericExportScope: SwiftGenericExportScope
//         get() = model.swiftGenericExportScope
// }

// data class SwiftKotlinTypeProtocolSirType(
//     val model: KotlinTypeSwiftModel,
// ) : SwiftNonNullReferenceSirType {
//     override val declaration: SwiftIrDeclaration
//         get() = model.swiftIrDeclaration
//     override val reference: SwiftFqName
//         get() = model.identifier
//
// }

object ObjcProtocolSirType: SwiftNonNullReferenceSirType {
    override val declaration: SwiftIrDeclaration = BuiltinDeclarations.Protocol

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedLocalTypeName("Protocol")
}
