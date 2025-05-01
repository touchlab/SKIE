package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.sir.element.SirValueParameter

class OirValueParameter(val label: String, val name: String, var type: OirType, val parent: OirFunction, val index: Int) : OirElement {

    // lateinit
    var originalSirValueParameter: SirValueParameter? = null

    init {
        parent.valueParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $label $name: $type"
}

fun OirFunction.copyValueParametersFrom(other: OirFunction) {
    copyValueParametersFrom(other.valueParameters)
}

fun OirFunction.copyValueParametersFrom(valueParameters: List<OirValueParameter>) {
    valueParameters.mapIndexed { index: Int, parameter: OirValueParameter ->
        OirValueParameter(
            label = parameter.label,
            name = parameter.name,
            // TODO Substitute type parameter usage
            type = parameter.type,
            parent = this,
            index = index,
        )
    }
}
