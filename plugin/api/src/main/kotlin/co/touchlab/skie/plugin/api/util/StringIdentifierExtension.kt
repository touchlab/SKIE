package co.touchlab.skie.plugin.api.util

fun String.toValidSwiftIdentifier(): String =
    this.map { it.takeIf { it.isLetterOrDigit() } ?: "_" }.joinToString("")
