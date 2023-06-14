package co.touchlab.skie.plugin.api.util

import org.jetbrains.kotlin.name.FqName

fun String.toValidSwiftIdentifier(): String =
    this.mapIndexed { index, char ->
        when {
            char.isLetter() -> char
            char.isDigit() -> if (index == 0) "_$char" else char
            else -> "_"
        }
    }.joinToString("")

fun FqName.toValidSwiftIdentifier(): String =
    this.asString().toValidSwiftIdentifier()
