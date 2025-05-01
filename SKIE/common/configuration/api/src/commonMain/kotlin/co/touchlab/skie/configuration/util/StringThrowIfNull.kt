package co.touchlab.skie.configuration.util

fun String?.throwIfNull(): String = this ?: throw IllegalStateException("Parsing error: value is not expected to be null")
