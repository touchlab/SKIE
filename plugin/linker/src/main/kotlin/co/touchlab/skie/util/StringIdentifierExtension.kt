package co.touchlab.skie.util

fun String.toValidSwiftIdentifier(): String =
    this.map { it.takeIf { it.isLetterOrDigit() } ?: "_" }.joinToString("")
