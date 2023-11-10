package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import io.outfoxx.swiftpoet.Modifier

class SirConstructor(
    parent: SirDeclarationNamespace,
    override var visibility: SirVisibility = SirVisibility.Public,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isConvenience: Boolean = false,
    override var throws: Boolean = false,
) : SirFunction(attributes.toMutableList(), modifiers.toMutableList()) {

    override val identifier = "init"

    override val reference: String
        get() = if (valueParameters.isEmpty()) {
            identifier
        } else {
            "${identifier}(${valueParameters.joinToString("") { "${it.labelOrName}:" }})"
        }

    override val name: String
        get() = if (valueParameters.isEmpty()) "${identifier}()" else reference

    override val scope: SirScope = SirScope.Global

    override var parent: SirDeclarationNamespace by sirDeclarationParent(parent)

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: $name"

    companion object {

        context(SirDeclarationNamespace)
        operator fun invoke(
            visibility: SirVisibility = SirVisibility.Public,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isConvenience: Boolean = false,
            throws: Boolean = false,
        ): SirConstructor =
            SirConstructor(
                parent = this@SirDeclarationNamespace,
                visibility = visibility,
                attributes = attributes,
                modifiers = modifiers,
                isConvenience = isConvenience,
                throws = throws,
            )
    }
}

fun SirConstructor.shallowCopy(
    parent: SirDeclarationNamespace = this.parent,
    visibility: SirVisibility = this.visibility,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
    isConvenience: Boolean = this.isConvenience,
    throws: Boolean = this.throws,
): SirConstructor =
    SirConstructor(
        parent = parent,
        visibility = visibility,
        attributes = attributes,
        modifiers = modifiers,
        isConvenience = isConvenience,
        throws = throws,
    )
