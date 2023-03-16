package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import io.outfoxx.swiftpoet.DeclaredTypeName

data class SwiftProtocolSirType(
    override val declaration: SwiftIrProtocolDeclaration,
) : SwiftNonNullReferenceSirType {
    override fun toSwiftPoetUsage(): DeclaredTypeName = declaration.internalName.toSwiftPoetName()

    override fun toString(): String = asString()
}
