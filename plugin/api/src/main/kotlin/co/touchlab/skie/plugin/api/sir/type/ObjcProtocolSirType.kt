package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

object ObjcProtocolSirType: SwiftNonNullReferenceSirType {
    override val declaration: SwiftIrDeclaration = BuiltinDeclarations.Protocol

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedLocalTypeName("Protocol")

    override fun toString(): String = asString()
}
