package co.touchlab.skie.sir.util

import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.type.DeclaredSirType

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
