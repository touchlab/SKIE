package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrDeclaration
import io.outfoxx.swiftpoet.TypeName

sealed interface SirType {

    val declaration: SwiftIrDeclaration

    val directChildren: List<SirType>

    fun toSwiftPoetUsage(): TypeName

    fun asString(): String {
        return toSwiftPoetUsage().toString()
    }
}

fun SirType.allChildrenRecursivelyAndThis(): List<SirType> =
    listOf(this) + directChildren.flatMap { it.allChildrenRecursivelyAndThis() }
