package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirValueParameterParent
import co.touchlab.skie.sir.type.SirType

class SirValueParameter(
    name: String,
    var type: SirType,
    parent: SirFunction,
    label: String? = null,
    var inout: Boolean = false,
) : SirElement {

    var parent: SirFunction by sirValueParameterParent(parent)

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
            label: String? = null,
            inout: Boolean = false,
        ): SirValueParameter =
            SirValueParameter(
                name = name,
                type = type,
                parent = this@SirFunction,
                label = label,
                inout = inout,
            )
    }
}
