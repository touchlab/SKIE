package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import io.outfoxx.swiftpoet.Modifier
import co.touchlab.skie.kir.element.DeprecationLevel

class SirConstructor(
    parent: SirDeclarationNamespace,
    override var visibility: SirVisibility = SirVisibility.Public,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isConvenience: Boolean = false,
    override var throws: Boolean = false,
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
) : SirFunction(attributes.toMutableList(), modifiers.toMutableList()) {

    override val identifier = "init"

    override val identifierAfterVisibilityChanges: String
        get() = identifier

    override val identifierForReference: String
        get() = identifier

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
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
        ): SirConstructor =
            SirConstructor(
                parent = this@SirDeclarationNamespace,
                visibility = visibility,
                attributes = attributes,
                modifiers = modifiers,
                isConvenience = isConvenience,
                throws = throws,
                deprecationLevel = deprecationLevel,
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
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
): SirConstructor =
    SirConstructor(
        parent = parent,
        visibility = visibility,
        attributes = attributes,
        modifiers = modifiers,
        isConvenience = isConvenience,
        throws = throws,
        deprecationLevel = deprecationLevel,
    )
