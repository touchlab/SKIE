package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope

// data class SwiftTypeSwiftModel(
//     override val containingType: TypeSwiftModel?,
//     override val identifier: SwiftTypeIdentifier,
//     val isHashable: Boolean,
// ) : TypeSwiftModel {
//
//     // override val stableFqName: SwiftFqName
//     //     get() {
//     //         TODO()
//     //         // return SwiftFqName.Local(TypeSwiftModel.StableFqNameNamespace + "swift__${fqName("_").name}")
//     //     }
//
//     // override val bridgedOrStableFqName: SwiftFqName
//     //     get() = stableFqName
//
//     override val isSwiftSymbol: Boolean = true
//     override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.None
//
//     // override fun fqName(separator: String): SwiftFqName.NominalType {
//     //     TODO()
//         // val parentName = containingType?.fqName(separator) ?: return SwiftFqName.Local(identifier)
//         //
//         // return SwiftFqName.Local("$parentName${separator}$identifier")
//     // }
// }
