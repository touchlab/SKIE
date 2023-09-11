package co.touchlab.skie.util.swift

fun String.toValidSwiftIdentifier(): String =
    this.mapIndexed { index, char ->
        when {
            char.isLetter() -> char
            char.isDigit() -> if (index == 0) "_$char" else char
            else -> "_"
        }
    }.joinToString("")
