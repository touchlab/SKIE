package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

object SwiftErrorSirType : SwiftNonNullReferenceSirType {

    override val declaration: SwiftIrDeclaration
        get() = TODO("Not yet implemented")

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName {
        TODO("Not yet implemented")
    }

    override fun toString(): String = asString()
}
