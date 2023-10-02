package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirPropertyAccessorParent
import io.outfoxx.swiftpoet.FunctionSpec

class SirGetter(
    property: SirProperty,
    attributes: List<String> = emptyList(),
) : SirElement, SirPropertyAccessor, SirElementWithAttributes, SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    override var property: SirProperty by sirPropertyAccessorParent(property)

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    companion object {

        context(SirProperty)
        operator fun invoke(
            attributes: List<String> = emptyList(),
        ): SirGetter =
            SirGetter(
                property = this@SirProperty,
                attributes = attributes,
            )
    }
}
