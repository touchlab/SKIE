package co.touchlab.skie.test.util

val KotlinVersion.needsOldLinker: Boolean
    get() = value.startsWith("1.8.") || value.startsWith("1.9.0")

val KotlinVersion.coroutinesVersion: String
    get() = when {
        value.startsWith("2.0.") -> "1.9.0-RC"
        value.startsWith("1.9.2") -> "1.8.1"
        value.startsWith("1.9.") || value.startsWith("1.8.2") -> "1.7.3"
        value.startsWith("1.8.") -> "1.6.4"
        else -> error("Coroutines version not assigned for Kotlin version $value")
    }
