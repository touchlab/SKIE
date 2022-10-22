package co.touchlab.swiftpack.api

data class SwiftBridgedName(
    val parent: SwiftTypeName?,
    val isNestedInParent: Boolean,
    val name: String,
) {
    val qualifiedName: String
        get() = if (parent == null) name else "${parent.qualifiedName}$separator$name"

    val typeAliasName: String
        get() = if (parent == null) name else "${parent.qualifiedNameWithSeparators(DEFAULT_SEPARATOR)}$typeAliasSeparator$name"

    // TODO: Not the best way to do this, but it works for now.
    val needsTypeAlias: Boolean
        get() = qualifiedName != typeAliasName

    private val separator: String
        get() = if (isNestedInParent) SwiftTypeName.DEFAULT_SEPARATOR else ""

    private val typeAliasSeparator: String
        get() = if (isNestedInParent) DEFAULT_SEPARATOR else ""

    companion object {
        const val DEFAULT_SEPARATOR = "__"
    }
}
