package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.oir.element.OirValueParameter
import co.touchlab.skie.sir.element.SirValueParameter
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

class KirValueParameter(
    val parent: KirFunction<*>,
    var type: KirType,
    val kind: Kind,
) : KirElement {

    val configuration: KirConfiguration = KirConfiguration(parent.configuration)

    lateinit var oirValueParameter: OirValueParameter

    val originalSirValueParameter: SirValueParameter?
        get() = oirValueParameter.originalSirValueParameter

    val descriptorOrNull: ParameterDescriptor?
        get() = when (kind) {
            is Kind.ValueParameter -> kind.descriptor
            else -> null
        }

    val name: String
        get() = when (kind) {
            is Kind.ValueParameter -> kind.descriptor.name.asString()
            Kind.Receiver -> "receiver"
            Kind.PropertySetterValue -> "value"
            Kind.ErrorOut -> "error"
            Kind.SuspendCompletion -> "completionHandler"
        }

    init {
        parent.valueParameters.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name: $type"

    sealed interface Kind {

        val descriptorOrNull: ParameterDescriptor?
            get() = null

        data class ValueParameter(val descriptor: ParameterDescriptor) : Kind {

            override val descriptorOrNull: ParameterDescriptor
                get() = descriptor
        }

        object Receiver : Kind

        object PropertySetterValue : Kind

        object ErrorOut : Kind

        object SuspendCompletion : Kind
    }
}
