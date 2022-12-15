package co.touchlab.skie.plugin.api.type

data class SwiftBridgedName(
    val parent: SwiftTypeName?,
    val name: String,
) {

    val qualifiedName: String
        get() = if (parent == null) name else "${parent.qualifiedName}${SwiftTypeName.DEFAULT_SEPARATOR}$name"

    val typeAliasName: String
        get() = if (parent == null) name else "${parent.qualifiedNameWithSeparators(DEFAULT_SEPARATOR)}$DEFAULT_SEPARATOR$name"

    // TODO: Not the best way to do this, but it works for now.
    val needsTypeAlias: Boolean
        get() = qualifiedName != typeAliasName

    companion object {

        const val DEFAULT_SEPARATOR = "__"
    }
}
