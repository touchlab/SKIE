@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.ValueParameterConfiguration
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.kir.type.translation.KirTypeParameterScope
import co.touchlab.skie.phases.KirPhase
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.valueParametersAssociated
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.resolve.isValueClass

internal abstract class BaseCreateRegularKirFunctionPhase(
    context: KirPhase.Context,
    supportsConstructors: Boolean = false,
    supportsSimpleFunctions: Boolean = false,
) : BaseCreateRegularKirMembersPhase(
    context = context,
    supportsConstructors = supportsConstructors,
    supportsSimpleFunctions = supportsSimpleFunctions,
) {

    context(KirTypeParameterScope)
    protected fun createValueParameters(
        function: KirFunction<*>,
        descriptor: FunctionDescriptor,
        methodBridge: MethodBridge,
    ) {
        methodBridge.valueParametersAssociated(descriptor)
            .forEach { (parameterBridge, parameterDescriptor) ->
                createValueParameter(parameterBridge, parameterDescriptor, function, descriptor)
            }
    }

    context(KirTypeParameterScope)
    private fun createValueParameter(
        parameterBridge: MethodBridgeValueParameter,
        parameterDescriptor: ParameterDescriptor?,
        function: KirFunction<*>,
        functionDescriptor: FunctionDescriptor,
    ) {
        val kind = when (parameterBridge) {
            is MethodBridgeValueParameter.Mapped -> when (parameterDescriptor) {
                null -> error("Mapped ValueParameter $parameterBridge has no descriptor.")
                is ReceiverParameterDescriptor -> KirValueParameter.Kind.Receiver
                is PropertySetterDescriptor -> KirValueParameter.Kind.PropertySetterValue
                else -> KirValueParameter.Kind.ValueParameter
            }
            MethodBridgeValueParameter.ErrorOutParameter -> KirValueParameter.Kind.ErrorOut
            is MethodBridgeValueParameter.SuspendCompletion -> KirValueParameter.Kind.SuspendCompletion
        }

        val kotlinName = when (kind) {
            is KirValueParameter.Kind.ValueParameter -> parameterDescriptor!!.name.asString()
            KirValueParameter.Kind.Receiver -> "receiver"
            KirValueParameter.Kind.PropertySetterValue -> "value"
            KirValueParameter.Kind.ErrorOut -> "error"
            KirValueParameter.Kind.SuspendCompletion -> "completionHandler"
        }

        val valueParameter = KirValueParameter(
            kotlinName = kotlinName,
            objCName = when (kind) {
                is KirValueParameter.Kind.ValueParameter -> namer.getParameterName(parameterDescriptor!!)
                else -> kotlinName
            }.toValidSwiftIdentifier(),
            parent = function,
            type = kirDeclarationTypeTranslator.mapValueParameterType(functionDescriptor, parameterDescriptor, parameterBridge),
            kind = kind,
            configuration = getValueParameterConfiguration(parameterDescriptor, function),
            wasTypeInlined = parameterDescriptor?.type?.constructor?.declarationDescriptor?.isValueClass() == true,
        )

        parameterDescriptor?.let {
            descriptorKirProvider.registerValueParameter(valueParameter, parameterDescriptor)
        }
    }

    private fun getValueParameterConfiguration(
        parameterDescriptor: ParameterDescriptor?,
        function: KirFunction<*>,
    ): ValueParameterConfiguration =
        if (parameterDescriptor != null) {
            descriptorConfigurationProvider.getConfiguration(parameterDescriptor)
        } else {
            ValueParameterConfiguration(function.configuration)
        }
}
