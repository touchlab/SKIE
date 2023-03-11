package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

sealed interface SirType {
    val declaration: SwiftIrDeclaration

    fun toSwiftPoetUsage(): TypeName

    fun asString(): String {
        return toSwiftPoetUsage().toString()
    }
}


//
// class SirSimpleType(
//     val constructor: SirTypeConstructor,
// ): SirType {
//     override val reference: TypeName
//         get() = TODO()
// }

class SirTypeConstructor(
    val declaration: SwiftIrDeclaration,
)

// class KotlinTypeModelIrTypeDeclaration(
//     val model: KotlinTypeSwiftModel,
// ): SwiftIrNominalTypeDeclaration {
//     override val swiftGenericExportScope: SwiftGenericExportScope
//         get() = model.swiftGenericExportScope
//
//     override val name: SwiftFqName.NominalType = model.identifier
//
//     override val containingDeclaration: SwiftIrExtensibleDeclaration?
//         get() = model.containingType?.swiftIrDeclaration
//
//     override val superTypes: List<SwiftIrExtensibleDeclaration>
//         get() = listOf(
//             // TODO: Actually it's `KotlinBase`, but we don't care right now
//             BuiltinSwiftDeclarations.nsObject,
//         )
// }


// sealed interface SwiftTypeModel : TypeSwiftModel {
//
//     override val swiftGenericExportScope: SwiftGenericExportScope
//         get() = SwiftGenericExportScope.None
//
//     override val containingType: TypeSwiftModel?
//         get() = null
//
//     override val identifier: SwiftTypeIdentifier
//         get() = stableFqName
//
//     override val bridgedOrStableFqName: SwiftFqName
//         get() = stableFqName
//
//     override val isSwiftSymbol: Boolean
//         get() = true
//
//     override fun fqName(separator: String): SwiftFqName {
//         return stableFqName
//     }
// }
