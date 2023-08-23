package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy

// TODO: Add support for verifying typeArguments are valid for the declaration
data class SwiftClassSirType(
    override val declaration: SwiftIrExtensibleDeclaration,
    val typeArguments: List<SwiftNonNullReferenceSirType> = emptyList(),
) : SwiftNonNullReferenceSirType {

    override val directChildren: List<SirType> = typeArguments

    override fun toSwiftPoetUsage(): TypeName {
        return if (typeArguments.isEmpty()) {
            declaration.internalName.toSwiftPoetName()
        } else {
            declaration.internalName.toSwiftPoetName().parameterizedBy(typeArguments.map { it.toSwiftPoetUsage() })
        }
    }

    override fun toString(): String = asString()
}
