package co.touchlab.skie.util.hash

import java.security.MessageDigest

fun String.hashed(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    val hash = digest.digest(toByteArray())

    return hash.joinToString("") { "%02x".format(it) }
}
