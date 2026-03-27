package co.touchlab.skie.acceptancetests.framework.util

// Do not forget to register test properties in the corresponding Gradle Plugin
object TestProperties {

    operator fun contains(name: String): Boolean =
        get(name) != null

    operator fun get(name: String): String? =
        System.getProperty(name) ?: System.getenv(name)
}
