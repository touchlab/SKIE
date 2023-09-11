package co.touchlab.skie.util.swift

import io.outfoxx.swiftpoet.DeclaredTypeName

fun DeclaredTypeName.Companion.qualifiedLocalTypeName(localTypeName: String): DeclaredTypeName =
    this.qualifiedTypeName(".$localTypeName")
