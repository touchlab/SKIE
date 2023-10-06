package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

class SirFunction(
    override var identifier: String,
    parent: SirDeclarationParent,
    var returnType: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = SirScope.Member,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isAsync: Boolean = false,
    var throws: Boolean = false,
) : SirCallableDeclaration, SirTypeParameterParent, SirValueParameterParent, SirOverridableDeclaration<SirFunction>,
    SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override val reference: String
        get() = if (valueParameters.isEmpty()) {
            identifierAfterVisibilityChanges
        } else {
            "$identifierAfterVisibilityChanges(${valueParameters.joinToString("") { "${it.labelOrName}:" }})"
        }

    override val name: String
        get() = if (valueParameters.isEmpty()) "$identifierAfterVisibilityChanges()" else reference

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    private val overridableDeclarationDelegate = SirOverridableDeclarationDelegate(this)

    override val memberOwner: SirClass? by overridableDeclarationDelegate::memberOwner

    override val overriddenDeclarations: List<SirFunction> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<SirFunction> by overridableDeclarationDelegate::overriddenBy

    override fun addOverride(declaration: SirFunction) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun removeOverride(declaration: SirFunction) {
        overridableDeclarationDelegate.removeOverride(declaration)
    }

    override fun addOverriddenBy(declaration: SirFunction) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun removeOverriddenBy(declaration: SirFunction) {
        overridableDeclarationDelegate.removeOverriddenBy(declaration)
    }

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    override fun toString(): String = "${this::class.simpleName}: $name"

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            returnType: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            scope: SirScope = SirScope.Member,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isAsync: Boolean = false,
            throws: Boolean = false,
        ): SirFunction =
            SirFunction(
                identifier = identifier,
                parent = this@SirDeclarationParent,
                returnType = returnType,
                visibility = visibility,
                scope = scope,
                attributes = attributes,
                modifiers = modifiers,
                isAsync = isAsync,
                throws = throws,
            )
    }
}

