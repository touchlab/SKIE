package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

class SirConstructor(
    parent: SirDeclarationNamespace,
    override var visibility: SirVisibility = SirVisibility.Public,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isConvenience: Boolean = false,
) : SirCallableDeclaration, SirValueParameterParent, SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override val identifier = "init"

    override val reference: String
        get() = if (valueParameters.isEmpty()) {
            identifier
        } else {
            "${identifier}(${valueParameters.joinToString("") { "${it.labelOrName}:" }})"
        }

    override val name: String
        get() = if (valueParameters.isEmpty()) "${identifier}()" else reference

    override val scope: SirScope = SirScope.Static

    override var parent: SirDeclarationNamespace by sirDeclarationParent(parent)

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    override fun toString(): String = "${this::class.simpleName}: $name"

    companion object {

        context(SirDeclarationNamespace)
        operator fun invoke(
            visibility: SirVisibility = SirVisibility.Public,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isConvenience: Boolean = false,
        ): SirConstructor =
            SirConstructor(
                parent = this@SirDeclarationNamespace,
                visibility = visibility,
                attributes = attributes,
                modifiers = modifiers,
                isConvenience = isConvenience,
            )
    }
}
