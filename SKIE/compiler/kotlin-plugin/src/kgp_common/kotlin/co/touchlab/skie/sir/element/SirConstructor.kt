package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

class SirConstructor(
    parent: SirDeclarationNamespace,
    var visibility: SirVisibility = SirVisibility.Public,
    modifiers: List<Modifier> = emptyList(),
    var isConvenience: Boolean = false,
) : SirDeclaration, SirValueParameterParent, SirElementWithModifiers, SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override var parent: SirDeclarationNamespace by sirDeclarationParent(parent)

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    companion object {

        context(SirDeclarationNamespace)
        operator fun invoke(
            visibility: SirVisibility = SirVisibility.Public,
            modifiers: List<Modifier> = emptyList(),
            isConvenience: Boolean = false,
        ): SirConstructor =
            SirConstructor(
                parent = this@SirDeclarationNamespace,
                visibility = visibility,
                modifiers = modifiers,
                isConvenience = isConvenience,
            )
    }
}
