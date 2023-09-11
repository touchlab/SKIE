package co.touchlab.skie.swiftmodel.callable.parameter

import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy

class OriginalKotlinValueParameterSwiftModel(
    delegate: KotlinValueParameterSwiftModel,
) : KotlinValueParameterSwiftModel by delegate {

    override val argumentLabel: String = delegate.argumentLabel

    override val flowMappingStrategy: FlowMappingStrategy = delegate.flowMappingStrategy

    // override val isChanged: Boolean = false
}
