package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

// data class SwiftNullableReferenceTypeModel(
//     val nonNullType: SwiftNonNullReferenceTypeModel,
//     val isNullableResult: Boolean = false,
// ) : SwiftReferenceTypeModel {
//
//     override val stableFqName: SwiftFqName
//         get() = SwiftFqName.Optional(nonNullType.stableFqName)
// }

data class SwiftNullableReferenceSirType(
    val nonNullType: SwiftNonNullReferenceSirType,
    val isNullableResult: Boolean = false,
) : SwiftReferenceSirType {
    override val declaration: SwiftIrDeclaration
        get() = nonNullType.declaration


    override fun toSwiftPoetUsage(): TypeName = nonNullType.toSwiftPoetUsage().makeOptional()
}
