package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.FunctionSpec

class SirFunction(
    var identifier: String,
    parent: SirDeclarationParent,
    var returnType: SirType,
    var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = SirScope.Member,
    overriddenDeclarations: List<SirFunction> = emptyList(),
    attributes: List<String> = emptyList(),
    var isAsync: Boolean = false,
    var throws: Boolean = false,
) : SirDeclaration, SirTypeParameterParent, SirValueParameterParent, SirElementWithAttributes, SirDeclarationWithScope,
    SirOverridableDeclaration<SirFunction>,
    SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    override val memberOwner: SirClass?
        get() = when (val parent = parent) {
            is SirClass -> parent
            is SirExtension -> parent.classDeclaration
            else -> null
        }

    override val overriddenDeclarations: MutableList<SirFunction> = overriddenDeclarations.toMutableList()

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            returnType: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            scope: SirScope = SirScope.Member,
            overriddenDeclarations: List<SirFunction> = emptyList(),
            attributes: List<String> = emptyList(),
            isAsync: Boolean = false,
            throws: Boolean = false,
        ): SirFunction =
            SirFunction(
                identifier = identifier,
                parent = this@SirDeclarationParent,
                returnType = returnType,
                visibility = visibility,
                scope = scope,
                overriddenDeclarations = overriddenDeclarations,
                attributes = attributes,
                isAsync = isAsync,
                throws = throws,
            )
    }
}

