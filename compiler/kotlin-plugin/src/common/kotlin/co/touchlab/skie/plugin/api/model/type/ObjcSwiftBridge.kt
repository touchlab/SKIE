package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration

sealed interface ObjcSwiftBridge {
    val declaration: SwiftIrTypeDeclaration

    data class FromSDK(
        override val declaration: SwiftIrTypeDeclaration,
    ): ObjcSwiftBridge

    data class FromSKIE(
        override val declaration: SwiftIrTypeDeclaration,
        val nestedTypealiasName: SwiftFqName.Local.Nested? = null,
    ): ObjcSwiftBridge
}
