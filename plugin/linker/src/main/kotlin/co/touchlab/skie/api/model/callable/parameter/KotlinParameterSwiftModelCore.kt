package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel.Origin
import co.touchlab.skie.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

internal class KotlinParameterSwiftModelCore(
    var argumentLabel: String,
    val parameterBridge: MethodBridgeParameter.ValueParameter,
    baseParameterDescriptor: ParameterDescriptor?,
    allArgumentLabels: List<String>,
) {

    fun getOrigin(parameterDescriptor: ParameterDescriptor?): Origin = when (parameterBridge) {
        is MethodBridgeParameter.ValueParameter.Mapped -> {
            when (parameterDescriptor) {
                is ValueParameterDescriptor -> Origin.ValueParameter(parameterDescriptor)
                is ReceiverParameterDescriptor -> Origin.Receiver(parameterDescriptor)
                null -> error("Mapped parameter does not have a descriptor: $parameterBridge")
                else -> error("Unknown parameter descriptor type: $parameterDescriptor")
            }
        }
        is MethodBridgeParameter.ValueParameter.SuspendCompletion -> Origin.SuspendCompletion
        is MethodBridgeParameter.ValueParameter.ErrorOutParameter -> error("ErrorOutParameter does not have a SwiftModel.")
    }

    /*
            Makes certain assumptions about the inner workings of method bridging.
                - Assumes that each function has at most one explicit receiver parameter
                - And that label of that parameter is "_"
            As a result it's safe for now to use argumentLabels as existingNames
        */
    val parameterName: String = when (val origin = getOrigin(baseParameterDescriptor)) {
        is Origin.Receiver -> {
            origin.descriptor.name.asStringStripSpecialMarkers().toValidSwiftIdentifier().collisionFreeIdentifier(allArgumentLabels)
        }
        is Origin.ValueParameter -> when (origin.descriptor) {
            is PropertySetterDescriptor -> "value"
            else -> origin.descriptor.name.asString().toValidSwiftIdentifier()
        }
        Origin.SuspendCompletion -> "completionHandler"
    }
}
