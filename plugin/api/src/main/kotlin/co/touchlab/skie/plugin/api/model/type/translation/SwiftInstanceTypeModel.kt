package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftInstanceSirType: SwiftNonNullReferenceSirType {

    override val declaration: SwiftIrDeclaration
        get() = TODO()

    override fun toSwiftPoetUsage(): TypeName = SelfTypeName.INSTANCE
}
