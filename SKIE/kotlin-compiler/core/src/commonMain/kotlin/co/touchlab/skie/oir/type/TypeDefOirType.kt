package co.touchlab.skie.oir.type

import co.touchlab.skie.oir.element.OirTypeDef

data class TypeDefOirType(val declaration: OirTypeDef, val typeArguments: List<OirType> = emptyList()) : OirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String = buildString {
        append(declaration.name)
        if (typeArguments.isNotEmpty()) {
            append("<")
            typeArguments.joinTo(this) { it.render("", false) }
            append(">")
        }
        appendAttrsAndName(attrsAndName)
    }
}
