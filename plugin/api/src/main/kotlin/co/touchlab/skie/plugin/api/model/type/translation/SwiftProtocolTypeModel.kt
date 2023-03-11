package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import io.outfoxx.swiftpoet.DeclaredTypeName

// data class SwiftProtocolTypeModel(
//     val protocolName: SwiftFqName,
// ) : SwiftNonNullReferenceTypeModel {
//
//     override val stableFqName: SwiftFqName
//         get() = protocolName
// }

data class SwiftProtocolSirType(
    override val declaration: SwiftIrProtocolDeclaration,
) : SwiftNonNullReferenceSirType {
    override fun toSwiftPoetUsage(): DeclaredTypeName = declaration.internalName.toSwiftPoetName()
}
