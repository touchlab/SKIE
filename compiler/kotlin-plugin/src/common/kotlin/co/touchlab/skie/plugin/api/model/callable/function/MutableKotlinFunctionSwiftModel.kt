package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel, MutableKotlinDirectlyCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel>

    override var returnTypeFlowMappingStrategy: FlowMappingStrategy
}
