package co.touchlab.skie.plugin.api.util

import io.outfoxx.swiftpoet.DeclaredTypeName

fun DeclaredTypeName.Companion.qualifiedLocalTypeName(localTypeName: String): DeclaredTypeName =
    this.qualifiedTypeName(".$localTypeName")
