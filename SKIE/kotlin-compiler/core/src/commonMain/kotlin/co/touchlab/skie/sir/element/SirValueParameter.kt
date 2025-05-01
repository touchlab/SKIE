package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirValueParameterParent
import co.touchlab.skie.sir.type.SirType

class SirValueParameter(
    name: String,
    var type: SirType,
    parent: SirFunction,
    attributes: List<String>,
    label: String? = null,
    var inout: Boolean = false,
    var defaultValue: String? = null,
) : SirElement,
    SirElementWithAttributes {

    var parent: SirFunction by sirValueParameterParent(parent)

    override var attributes: MutableList<String> = attributes.toMutableList()

    var name: String = name
        set(value) {
            field = value

            label = label
        }

    var label: String? = null
        set(value) {
            field = if (value != name && value?.isNotBlank() == true) value else null
        }

    init {
        // Calls setter
        this.label = label
    }

    val labelOrName: String
        get() = label ?: name

    override fun toString(): String = "${this::class.simpleName}:${label?.let { " $it" } ?: ""} $name: $type"

    companion object {

        context(SirFunction)
        operator fun invoke(
            name: String,
            type: SirType,
            attributes: List<String> = emptyList(),
            label: String? = null,
            inout: Boolean = false,
            defaultValue: String? = null,
        ): SirValueParameter = SirValueParameter(
            name = name,
            type = type,
            parent = this@SirFunction,
            attributes = attributes,
            label = label,
            inout = inout,
            defaultValue = defaultValue,
        )
    }
}
