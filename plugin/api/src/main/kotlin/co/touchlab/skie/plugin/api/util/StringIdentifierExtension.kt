package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.name.FqName

fun String.toValidSwiftIdentifier(): String =
    this.map { it.takeIf { it.isLetterOrDigit() } ?: "_" }.joinToString("")

fun FqName.toValidSwiftIdentifier(): String =
    this.asString().toValidSwiftIdentifier()
