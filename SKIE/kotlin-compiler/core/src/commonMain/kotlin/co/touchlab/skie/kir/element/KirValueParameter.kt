package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.ValueParameterConfiguration
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.oir.element.OirValueParameter
import co.touchlab.skie.sir.element.SirValueParameter

class KirValueParameter(
    val kotlinName: String,
    val objCName: String,
    val parent: KirFunction<*>,
    var type: KirType,
    val kind: Kind,
    val configuration: ValueParameterConfiguration,
    val wasTypeInlined: Boolean,
) : KirElement {

    lateinit var oirValueParameter: OirValueParameter

    val originalSirValueParameter: SirValueParameter?
        get() = oirValueParameter.originalSirValueParameter

    init {
        parent.valueParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $kotlinName: $type"

    sealed interface Kind {

        object ValueParameter : Kind

        object Receiver : Kind

        object PropertySetterValue : Kind

        object ErrorOut : Kind

        object SuspendCompletion : Kind
    }
}
