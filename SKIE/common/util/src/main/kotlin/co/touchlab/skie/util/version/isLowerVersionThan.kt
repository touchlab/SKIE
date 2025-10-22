package co.touchlab.skie.util.version

fun String.isLowerVersionThan(other: String): Boolean =
    isLowerVersionThan(this.split("."), other.split("."))

private fun isLowerVersionThan(lhs: List<String>, rhs: List<String>): Boolean =
    when {
        lhs.isEmpty() && rhs.isEmpty() -> false
        lhs.isEmpty() -> isLowerVersionThan(listOf("0"), rhs)
        rhs.isEmpty() -> isLowerVersionThan(rhs, listOf("0"))
        lhs.first().isLowerVersionComponentThan(rhs.first()) -> true
        rhs.first().isLowerVersionComponentThan(lhs.first()) -> false
        else -> isLowerVersionThan(lhs.drop(1), rhs.drop(1))
    }

private fun String.isLowerVersionComponentThan(other: String): Boolean {
    val lhsIntVersion = this.toIntOrNull() ?: 0
    val rhsIntVersion = other.toIntOrNull() ?: 0

    return lhsIntVersion < rhsIntVersion
}
