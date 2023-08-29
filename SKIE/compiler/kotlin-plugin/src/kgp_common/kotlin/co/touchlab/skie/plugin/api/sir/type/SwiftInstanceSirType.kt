package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftInstanceSirType : SwiftNonNullReferenceSirType {

    override val declaration: SwiftIrDeclaration
        get() = TODO()

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName = SelfTypeName.INSTANCE

    override fun toString(): String = asString()
}
