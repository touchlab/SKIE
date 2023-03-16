package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations

object SwiftAnyHashableSirType: SwiftNonNullReferenceSirType {
    override val declaration = BuiltinDeclarations.Swift.AnyHashable
    override fun toSwiftPoetUsage() = declaration.internalName.toSwiftPoetName()

    override fun toString(): String = asString()
}
