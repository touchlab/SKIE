package co.touchlab.skie.plugin.libraries.library

data class Module(
    val group: String,
    val name: String,
) {

    val fqName: String = "$group:$name"

    override fun toString(): String = fqName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Module

        if (group.lowercase() != other.group.lowercase()) return false
        if (name.lowercase() != other.name.lowercase()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.lowercase().hashCode()
        result = 31 * result + name.lowercase().hashCode()
        return result
    }
}
