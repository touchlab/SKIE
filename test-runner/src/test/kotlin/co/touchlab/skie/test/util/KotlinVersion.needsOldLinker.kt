package co.touchlab.skie.test.util

val KotlinVersion.needsOldLinker: Boolean
    get() = value.startsWith("1.8.") || value.startsWith("1.9.0")
