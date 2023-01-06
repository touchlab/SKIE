package co.touchlab.skie.util

fun String.sanitizeForIdentifier(): String =
    this.map { it.takeIf { it.isLetterOrDigit() } ?: "_" }.joinToString("")
