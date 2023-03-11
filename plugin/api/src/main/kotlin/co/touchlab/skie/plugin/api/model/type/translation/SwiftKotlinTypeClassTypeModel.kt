package co.touchlab.skie.plugin.api.model.type.translation

// data class SwiftKotlinTypeClassTypeModel(
//     val model: KotlinClassSwiftModel,
//     val typeArguments: List<SwiftNonNullReferenceTypeModel> = emptyList(),
// ) : SwiftNonNullReferenceTypeModel {
//
//     override val stableFqName: SwiftFqName.NominalType
//         get() = if (typeArguments.isEmpty()) {
//             model.stableFqName
//         } else {
//             model.stableFqName.withTypeArguments(typeArguments.map { it.stableFqName })
//         }
//
//     override val bridgedOrStableFqName: SwiftFqName
//         get() = model.bridgedOrStableFqName
//     override val isSwiftSymbol: Boolean
//         get() = model.isSwiftSymbol
//
//     override fun fqName(separator: String): SwiftFqName = model.fqName(separator)
//
//     override val containingType: KotlinClassSwiftModel?
//         get() = model.containingType
//     override val identifier: String
//         get() = model.identifier
//     override val swiftGenericExportScope: SwiftGenericExportScope
//         get() = model.swiftGenericExportScope
// }

// data class SwiftKotlinTypeClassSirType(
//     override val declaration: KotlinTypeModelIrTypeDeclaration,
//     val typeArguments: List<SwiftNonNullReferenceSirType> = emptyList(),
// ) : SwiftNonNullReferenceSirType {
//
//     override val reference: SwiftFqName
//         get() = if (typeArguments.isEmpty()) {
//             declaration.model.identifier
//         } else {
//             declaration.model.identifier.withTypeArguments(typeArguments.map { it.reference })
//         }
// }
