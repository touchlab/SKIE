package co.touchlab.skie.oir.type

sealed class OirType {

    fun render(): String = render("", true)

    abstract fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String

    protected fun String.withAttrsAndName(attrsAndName: String) = "${this.trim()} ${attrsAndName.trim()}".trim()

    protected fun String.plusNonnullAttributeIfNeeded(needsNonnullAttribute: Boolean) =
        if (needsNonnullAttribute) OBJC_NONNULL_ATTRIBUTE.withAttrsAndName(this) else this

    protected fun StringBuilder.appendAttrsAndName(attrsAndName: String) {
        if (attrsAndName.isNotBlank()) {
            append(' ')
            append(attrsAndName.trim())
        }
    }

    override fun toString(): String = render()

    companion object {

        const val OBJC_NONNULL_ATTRIBUTE: String = "_Nonnull"
        const val OBJC_NULLABLE_ATTRIBUTE = "_Nullable"
        const val OBJC_NULLABLE_RESULT_ATTRIBUTE = "_Nullable_result"
    }
}
