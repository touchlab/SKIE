package co.touchlab.skie.oir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirTypeDeclaration
import co.touchlab.skie.oir.element.OirTypeDef

data class DeclaredOirType(
    val declaration: OirTypeDeclaration,
    val typeArguments: List<OirType> = emptyList(),
) : NonNullReferenceOirType() {

    init {
        require(declaration is OirClass || typeArguments.isEmpty()) {
            "Type arguments are only allowed for classes."
        }
    }

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        when (declaration) {
            is OirClass -> renderClass(declaration, attrsAndName, needsNonnullAttribute)
            is OirTypeDef -> renderTypeDef(declaration, attrsAndName, needsNonnullAttribute)
        }

    private fun renderClass(declaration: OirClass, attrsAndName: String, needsNonnullAttribute: Boolean): String =
        when (declaration.kind) {
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

    private fun renderTypeDef(declaration: OirTypeDef, attrsAndName: String, needsNonnullAttribute: Boolean): String =
        declaration.name.withAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
}
