package co.touchlab.skie.plugin.util

fun lowerCamelCaseName(vararg nameParts: String?): String {
    val nonEmptyParts = nameParts.mapNotNull { it?.takeIf(String::isNotEmpty) }

    return nonEmptyParts.drop(1).joinToString(
        separator = "",
        prefix = nonEmptyParts.firstOrNull().orEmpty(),
        transform = String::capitalizeAsciiOnly,
    )
}

private fun String.capitalizeAsciiOnly(): String {
    if (isEmpty()) {
        return this
    }

    val c = this[0]

    return if (c in 'a'..'z') {
        c.uppercaseChar() + substring(1)
    } else {
        this
    }
}
