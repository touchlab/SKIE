package co.touchlab.skie.oir.type

import co.touchlab.skie.oir.element.OirClass

data class DeclaredOirType(val declaration: OirClass, val typeArguments: List<OirType> = emptyList()) : NonNullReferenceOirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String = when (declaration.kind) {
        OirClass.Kind.Class -> {
            buildString {
                append(declaration.name)
                if (typeArguments.isNotEmpty()) {
                    append("<")
                    typeArguments.joinTo(this) { it.render("", false) }
                    append(">")
                }
                append(" *")
                appendAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
            }
        }
        OirClass.Kind.Protocol -> {
            "id<${declaration.name}>".withAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
        }
    }
}
