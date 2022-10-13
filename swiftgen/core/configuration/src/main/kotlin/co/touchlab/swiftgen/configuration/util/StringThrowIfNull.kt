package co.touchlab.swiftgen.configuration.util

internal fun String?.throwIfNull(): String =
    this ?: throw IllegalStateException("Parsing error: value is not expected to be null")