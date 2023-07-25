package co.touchlab.skie.util

import java.security.MessageDigest
import java.util.Base64

fun String.hashed(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    val hash = digest.digest(toByteArray())

    return Base64.getEncoder().encodeToString(hash)
}

