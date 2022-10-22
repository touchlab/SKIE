package co.touchlab.swiftpack.api

interface SwiftTypeName {
    val parent: SwiftTypeName?
    val isNestedInParent: Boolean
    val simpleName: String
    val originalSimpleName: String

    val originalQualifiedName: String
    val qualifiedName: String

    fun qualifiedNameWithSeparators(separator: String = DEFAULT_SEPARATOR): String

    companion object {
        const val DEFAULT_SEPARATOR = "."
    }
}
