package co.touchlab.skie.plugin.api.sir.util

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.element.SirTypeAlias
import co.touchlab.skie.plugin.api.sir.type.DeclaredSirType

context(SwiftModelScope)
fun SirClass.isObjcBridgeable(): Boolean =
    this == sirBuiltins.Swift._ObjectiveCBridgeable || superTypes.any { it.isObjcBridgeable() }

context(SwiftModelScope)
private fun DeclaredSirType.isObjcBridgeable(): Boolean =
    when (val declaration = declaration) {
        is SirClass -> declaration.isObjcBridgeable()
        is SirTypeAlias -> declaration.isObjcBridgeable()
    }

context(SwiftModelScope)
private fun SirTypeAlias.isObjcBridgeable(): Boolean =
    when (val type = type) {
        is DeclaredSirType -> type.isObjcBridgeable()
        else -> false
    }
