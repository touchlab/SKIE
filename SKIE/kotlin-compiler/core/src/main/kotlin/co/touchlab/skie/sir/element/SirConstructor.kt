package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.Modifier

class SirConstructor(
    parent: SirDeclarationNamespace,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var isHidden: Boolean = false,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isConvenience: Boolean = false,
    override var throws: Boolean = false,
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
    override var isWrappedBySkie: Boolean = false,
) : SirFunction(attributes.toMutableList(), modifiers.toMutableList()) {

    override val identifier = "init"

    override val identifierAfterVisibilityChange: String
        get() = identifier

    override val identifierForReference: String
        get() = identifier

    override val scope: SirScope = SirScope.Global

    override var parent: SirDeclarationNamespace by sirDeclarationParent(parent)

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override val isReplaced: Boolean = false

    val returnType: SirType
        get() = parent.classDeclaration.toType(parent.classDeclaration.typeParameters.map { it.toTypeParameterUsage() })

    companion object {

        context(SirDeclarationNamespace)
        operator fun invoke(
            visibility: SirVisibility = SirVisibility.Public,
            isHidden: Boolean = false,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isConvenience: Boolean = false,
            throws: Boolean = false,
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
            isWrappedBySkie: Boolean = false,
        ): SirConstructor =
            SirConstructor(
                parent = this@SirDeclarationNamespace,
                visibility = visibility,
                isHidden = isHidden,
                attributes = attributes,
                modifiers = modifiers,
                isConvenience = isConvenience,
                throws = throws,
                deprecationLevel = deprecationLevel,
                isWrappedBySkie = isWrappedBySkie,
            )
    }
}

fun SirConstructor.shallowCopy(
    parent: SirDeclarationNamespace = this.parent,
    visibility: SirVisibility = this.visibility,
    isHidden: Boolean = this.isHidden,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
    isConvenience: Boolean = this.isConvenience,
    throws: Boolean = this.throws,
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
    isWrappedBySkie: Boolean = false,
): SirConstructor =
    SirConstructor(
        parent = parent,
        visibility = visibility,
        isHidden = isHidden,
        attributes = attributes,
        modifiers = modifiers,
        isConvenience = isConvenience,
        throws = throws,
        deprecationLevel = deprecationLevel,
        isWrappedBySkie = isWrappedBySkie,
    )
