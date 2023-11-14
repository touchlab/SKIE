package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirPropertyAccessorParent
import io.outfoxx.swiftpoet.FunctionSpec

class SirGetter(
    property: SirProperty,
    var throws: Boolean = false,
    attributes: List<String> = emptyList(),
) : SirElement, SirPropertyAccessor {

    override var property: SirProperty by sirPropertyAccessorParent(property)

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val bodyBuilder = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    override fun toString(): String = "${this::class.simpleName}: ($property)"

    companion object {

        context(SirProperty)
        operator fun invoke(
            throws: Boolean = false,
            attributes: List<String> = emptyList(),
        ): SirGetter =
            SirGetter(
                property = this@SirProperty,
                throws = throws,
                attributes = attributes,
            )
    }
}
