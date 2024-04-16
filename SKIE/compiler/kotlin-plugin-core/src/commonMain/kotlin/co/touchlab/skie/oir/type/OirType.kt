package co.touchlab.skie.oir.type

sealed class OirType {

    fun render(): String =
        render("", true)

    abstract fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String

    protected fun String.withAttrsAndName(attrsAndName: String) =
        "${this.trim()} ${attrsAndName.trim()}".trim()

    protected fun String.plusNonnullAttributeIfNeeded(needsNonnullAttribute: Boolean) =
        if (needsNonnullAttribute) objcNonnullAttribute.withAttrsAndName(this) else this

    protected fun StringBuilder.appendAttrsAndName(attrsAndName: String) {
        if (attrsAndName.isNotBlank()) {
            append(' ')
            append(attrsAndName.trim())
        }
    }

    override fun toString(): String =
        render()

    companion object {

        const val objcNonnullAttribute: String = "_Nonnull"
        const val objcNullableAttribute = "_Nullable"
        const val objcNullableResultAttribute = "_Nullable_result"
    }
}


