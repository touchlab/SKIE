package co.touchlab.skie.plugin.api.type

interface SwiftTypeName {
    val parent: SwiftTypeName?
    val originalParent: SwiftTypeName?

    val simpleName: String
    val originalSimpleName: String

    val qualifiedName: String
    val originalQualifiedName: String

    fun qualifiedNameWithSeparators(separator: String = DEFAULT_SEPARATOR): String {
        val parentName = parent?.qualifiedNameWithSeparators(separator) ?: return simpleName
        return "$parentName${separator}$simpleName"
    }

    fun originalQualifiedNameWithSeparators(separator: String = SwiftTypeName.DEFAULT_SEPARATOR): String {
        val parentName = originalParent?.originalQualifiedNameWithSeparators(separator) ?: return originalSimpleName
        return "$parentName${separator}$originalSimpleName"
    }

    companion object {
        const val DEFAULT_SEPARATOR = "."
    }
}
