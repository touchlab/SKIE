@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.function

import co.touchlab.skie.api.model.parameter.OriginalKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.valueParametersAssociated
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

abstract class BaseKotlinFunctionSwiftModel(
    final override val descriptor: FunctionDescriptor,
    override val receiver: KotlinTypeSwiftModel,
    private val namer: ObjCExportNamer,
) : KotlinFunctionSwiftModel {

    override val identifier: String
        get() = originalIdentifier

    override val parameters: List<KotlinParameterSwiftModel>
        get() = originalParameters

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val objCSelector: String = namer.getSelector(descriptor)

    private val originalIdentifier: String

    private val originalParameters: List<KotlinParameterSwiftModel>

    init {
        val (identifier, argumentLabels) = descriptor.swiftNameComponents
        originalIdentifier = identifier
        originalParameters = descriptor.getParametersSwiftModels(argumentLabels)
    }

    private val FunctionDescriptor.swiftNameComponents: Pair<String, List<String>>
        get() {
            val swiftName = namer.getSwiftName(this)

            val (identifier, argumentLabelsString) = swiftNameComponentsRegex.matchEntire(swiftName)?.destructured
                ?: error("Unable to parse swift name: $swiftName")

            val argumentLabels = argumentLabelsString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

            return identifier to argumentLabels
        }

    private fun FunctionDescriptor.getParametersSwiftModels(argumentLabels: List<String>): List<KotlinParameterSwiftModel> =
        namer.mapper
            .bridgeMethod(this)
            .valueParametersAssociated(this)
            .zip(argumentLabels)
            .map { (parameterBridgeWithDescriptor, argumentLabel) ->
                OriginalKotlinParameterSwiftModel(
                    origin = parameterBridgeWithDescriptor.origin,
                    argumentLabel = argumentLabel,
                    parameterName = parameterBridgeWithDescriptor.getParameterName(argumentLabel, argumentLabels),
                )
            }

    private val Pair<MethodBridgeValueParameter, ParameterDescriptor?>.origin: KotlinParameterSwiftModel.Origin
        get() = when (this.first) {
            is MethodBridgeValueParameter.Mapped -> {
                when (val parameterDescriptor = this.second) {
                    is ValueParameterDescriptor -> KotlinParameterSwiftModel.Origin.ValueParameter(parameterDescriptor)
                    is ReceiverParameterDescriptor -> KotlinParameterSwiftModel.Origin.Receiver(parameterDescriptor)
                    null -> error("Mapped parameter does not have a descriptor: $this")
                    else -> error("Unknown parameter descriptor type: $parameterDescriptor")
                }
            }
            is MethodBridgeValueParameter.SuspendCompletion -> KotlinParameterSwiftModel.Origin.SuspendCompletion
            is MethodBridgeValueParameter.ErrorOutParameter -> KotlinParameterSwiftModel.Origin.ErrorOutParameter
        }

    /*
        Makes certain assumptions about the inner workings of method bridging.
            - Assumes that each function has at most one explicit receiver parameter
            - And that label of that parameter is "_"
        As a result it's safe for now to use argumentLabels as existingNames
    */
    private fun Pair<MethodBridgeValueParameter, ParameterDescriptor?>.getParameterName(
        argumentLabel: String,
        existingNames: List<String>,
    ): String = when (this.second) {
        is ReceiverParameterDescriptor -> this.second!!.name.asStringStripSpecialMarkers().collisionFreeIdentifier(existingNames)
        else -> argumentLabel
    }

    private companion object {

        val swiftNameComponentsRegex = "(.+?)\\((.*?)\\)".toRegex()
    }
}
