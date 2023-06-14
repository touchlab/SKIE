package co.touchlab.skie.util

import java.security.MessageDigest
import java.util.Base64

fun String.redacted(): String = "<redacted>"

fun String.redactedIfNotNumberOrBoolean(): String =
    when {
        toLongOrNull() != null -> this
        toDoubleOrNull() != null -> this
        lowercase().toBooleanStrictOrNull() != null -> this
        else -> redacted()
    }


fun List<String>.redacted(): List<String> =
    map { it.redacted() }

fun Map<String, String>.redacted(): Map<String, String> =
    mapKeys { it.key.redacted() }.mapValues { it.value.redacted() }

fun String.hashed(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    val hash = digest.digest(toByteArray())

    return Base64.getEncoder().encodeToString(hash)
}

