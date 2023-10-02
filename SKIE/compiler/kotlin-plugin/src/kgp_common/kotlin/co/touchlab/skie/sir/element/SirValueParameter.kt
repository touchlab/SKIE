package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirValueParameterParent
import co.touchlab.skie.sir.type.SirType

class SirValueParameter(
    var name: String,
    var type: SirType,
    parent: SirValueParameterParent,
    var label: String? = null,
    var inout: Boolean = false,
) : SirElement {

    var parent: SirValueParameterParent by sirValueParameterParent(parent)

    // WIP Add missing toStrings

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
