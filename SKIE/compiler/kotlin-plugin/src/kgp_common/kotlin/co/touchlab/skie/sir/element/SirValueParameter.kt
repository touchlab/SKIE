package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirValueParameterParent
import co.touchlab.skie.sir.type.SirType

class SirValueParameter(
    name: String,
    var type: SirType,
    parent: SirValueParameterParent,
    label: String? = null,
    var inout: Boolean = false,
) : SirElement {

    var parent: SirValueParameterParent by sirValueParameterParent(parent)

    var name: String = name
        set(value) {
            field = value

            label = label
        }

    var label: String? = if (label != name) label else null
        set(value) {
            field = if (value != name) value else null
        }

    val labelOrName: String
        get() = label ?: name

    // WIP 2 Add missing toStrings

    override fun toString(): String = "${this::class.simpleName}: $label $name: $type"

    companion object {

        context(SirValueParameterParent)
        operator fun invoke(
            name: String,
            type: SirType,
            label: String? = null,
            inout: Boolean = false,
        ): SirValueParameter =
            SirValueParameter(
                name = name,
                type = type,
                parent = this@SirValueParameterParent,
                label = label,
                inout = inout,
            )
    }
}
