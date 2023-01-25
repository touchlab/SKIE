package co.touchlab.skie.plugin.api.model.type.bridge

import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.allParameters

data class MethodBridge(
    val returnBridge: ReturnValue,
    val receiver: MethodBridgeParameter.Receiver,
    val valueParameters: List<MethodBridgeParameter.ValueParameter>
) {

    sealed class ReturnValue {
        object Void : ReturnValue()
        object HashCode : ReturnValue()
        data class Mapped(val bridge: NativeTypeBridge) : ReturnValue()
        sealed class Instance : ReturnValue() {
            object InitResult : Instance()
            object FactoryResult : Instance()
        }

        sealed class WithError : ReturnValue() {
            object Success : WithError()
            data class ZeroForError(val successBridge: ReturnValue, val successMayBeZero: Boolean) : WithError()
        }

        object Suspend : ReturnValue()
    }

    val paramBridges: List<MethodBridgeParameter> =
        listOf(receiver) + MethodBridgeParameter.Selector + valueParameters

    // TODO: it is not exactly true in potential future cases.
    val isInstance: Boolean get() = when (receiver) {
        MethodBridgeParameter.Receiver.Static,
        MethodBridgeParameter.Receiver.Factory
        -> false

        MethodBridgeParameter.Receiver.Instance -> true
    }

    val returnsError: Boolean
        get() = returnBridge is ReturnValue.WithError
}

fun MethodBridge.valueParametersAssociated(
    descriptor: FunctionDescriptor
): List<Pair<MethodBridgeParameter.ValueParameter, ParameterDescriptor?>> {
    val kotlinParameters = descriptor.allParameters.iterator()
    val skipFirstKotlinParameter = when (this.receiver) {
        MethodBridgeParameter.Receiver.Static -> false
        MethodBridgeParameter.Receiver.Factory, MethodBridgeParameter.Receiver.Instance -> true
    }
    if (skipFirstKotlinParameter) {
        kotlinParameters.next()
    }

    return this.valueParameters.map {
        when (it) {
            is MethodBridgeParameter.ValueParameter.Mapped -> it to kotlinParameters.next()

            is MethodBridgeParameter.ValueParameter.SuspendCompletion,
            is MethodBridgeParameter.ValueParameter.ErrorOutParameter
            -> it to null
        }
    }.also { assert(!kotlinParameters.hasNext()) }
}

fun MethodBridge.parametersAssociated(
    irFunction: IrFunction
): List<Pair<MethodBridgeParameter, IrValueParameter?>> {
    val kotlinParameters = irFunction.allParameters.iterator()

    return this.paramBridges.map {
        when (it) {
            is MethodBridgeParameter.ValueParameter.Mapped, MethodBridgeParameter.Receiver.Instance ->
                it to kotlinParameters.next()

            is MethodBridgeParameter.ValueParameter.SuspendCompletion,
            MethodBridgeParameter.Receiver.Static, MethodBridgeParameter.Selector, MethodBridgeParameter.ValueParameter.ErrorOutParameter
            ->
                it to null

            MethodBridgeParameter.Receiver.Factory -> {
                kotlinParameters.next()
                it to null
            }
        }
    }.also { assert(!kotlinParameters.hasNext()) }
}
