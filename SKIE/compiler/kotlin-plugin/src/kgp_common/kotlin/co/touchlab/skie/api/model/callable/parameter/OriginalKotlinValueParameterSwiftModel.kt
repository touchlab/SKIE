package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy

class OriginalKotlinValueParameterSwiftModel(
    delegate: KotlinValueParameterSwiftModel,
) : KotlinValueParameterSwiftModel by delegate {

    override val argumentLabel: String = delegate.argumentLabel

    override val flowMappingStrategy: FlowMappingStrategy = delegate.flowMappingStrategy

    // override val isChanged: Boolean = false
}
