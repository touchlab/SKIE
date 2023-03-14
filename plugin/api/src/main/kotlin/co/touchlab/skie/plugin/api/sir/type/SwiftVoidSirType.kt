package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftVoidSirType: SirType {
    override val declaration: SwiftIrDeclaration = BuiltinDeclarations.Void

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedTypeName("Swift.Void")

}
