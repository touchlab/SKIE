package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirPropertyAccessorParent
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

class SirSetter(
    property: SirProperty,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var parameterName: String = "value",
) : SirElement, SirPropertyAccessor, SirElementWithAttributes, SirElementWithModifiers,
    SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override var property: SirProperty by sirPropertyAccessorParent(property)

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    companion object {

        context(SirProperty)
        operator fun invoke(
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
        ): SirSetter =
            SirSetter(
                property = this@SirProperty,
                attributes = attributes,
                modifiers = modifiers,
            )
    }
}
