package co.touchlab.skie.test.util

val KotlinVersion.coroutinesVersion: String
    get() = when {
        value.startsWith("2.") -> "1.9.0"
        else -> error("Coroutines version not assigned for Kotlin version $value")
    }
