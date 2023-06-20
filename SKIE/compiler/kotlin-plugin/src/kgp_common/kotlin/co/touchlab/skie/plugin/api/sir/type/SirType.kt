package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

sealed interface SirType {
    val declaration: SwiftIrDeclaration

    fun toSwiftPoetUsage(): TypeName

    fun asString(): String {
        return toSwiftPoetUsage().toString()
    }
}
