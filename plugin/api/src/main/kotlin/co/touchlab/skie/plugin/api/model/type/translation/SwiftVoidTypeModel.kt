package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

// object SwiftVoidTypeModel : SwiftTypeModel {
//
//     override val stableFqName: SwiftFqName.Special.Void
//         get() = SwiftFqName.Special.Void
// }

object SwiftVoidSirType: SirType {
    override val declaration: SwiftIrDeclaration = BuiltinDeclarations.Void

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedTypeName("Swift.Void")

}
