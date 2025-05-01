package co.touchlab.skie.util

object SystemProperty {

    fun find(name: String): String? = System.getProperty(name)

    fun get(name: String): String = find(name) ?: error("System property '$name' not found.")

    fun exists(name: String): Boolean = find(name) != null

    fun notExists(name: String): Boolean = find(name) == null
}
