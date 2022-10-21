package co.touchlab.swiftpack.api

class DefaultMutableSwiftTypeName(
    private val originalParent: MutableSwiftTypeName?,
    private val originalSeparator: String,
    override val originalSimpleName: String,
): MutableSwiftTypeName {
    override var parent: MutableSwiftTypeName? = originalParent
    override var separator: String = originalSeparator
    override var simpleName = originalSimpleName

    override val isChanged: Boolean
        get() = simpleName != originalSimpleName || separator != originalSeparator || parent != originalParent || parent?.isChanged == true

    override val originalQualifiedName: String
        get() {
            val parentName = parent?.originalQualifiedName ?: return originalSimpleName
            return "$parentName$separator$originalSimpleName"
        }

    override val qualifiedName: String
        get() {
            val parentName = parent?.qualifiedName ?: return simpleName
            return "$parentName$separator$simpleName"
        }
}
