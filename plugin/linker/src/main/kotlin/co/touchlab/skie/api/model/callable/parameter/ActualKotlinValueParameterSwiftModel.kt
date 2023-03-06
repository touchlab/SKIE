package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel.Origin
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

internal class ActualKotlinValueParameterSwiftModel(
    private val core: KotlinParameterSwiftModelCore,
    private val functionDescriptor: FunctionDescriptor,
    private val parameterDescriptor: ParameterDescriptor?,
    override val position: Int,
    private val getParameterType: (flowMappingStrategy: FlowMappingStrategy) -> TypeSwiftModel,
) : MutableKotlinValueParameterSwiftModel {

    override val origin: Origin = core.getOrigin(parameterDescriptor)

    override var argumentLabel: String by core::argumentLabel

    override val parameterName: String by core::parameterName

    override val original: KotlinValueParameterSwiftModel = OriginalKotlinValueParameterSwiftModel(this)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel || flowMappingStrategy != original.flowMappingStrategy

    override val type: TypeSwiftModel
        get() = getParameterType(flowMappingStrategy)

    override val objCType: ObjCType
        get() = core.getObjCType(functionDescriptor, parameterDescriptor, flowMappingStrategy)

    override var flowMappingStrategy: FlowMappingStrategy = FlowMappingStrategy.None

    override fun toString(): String = origin.toString()
}
