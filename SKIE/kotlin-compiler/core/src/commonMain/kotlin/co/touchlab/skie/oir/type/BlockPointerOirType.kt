package co.touchlab.skie.oir.type

data class BlockPointerOirType(
    val valueParameterTypes: List<OirType>,
    val returnType: OirType,
) : NonNullReferenceOirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        returnType.render(
            buildString {
                append("(^")
                appendAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
                append(")(")
                if (valueParameterTypes.isEmpty()) append("void")
                valueParameterTypes.joinTo(this) { it.render("", true) }
                append(')')
            },
            true,
        )
}
