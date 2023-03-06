package co.touchlab.skie.plugin.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy

interface MutableKotlinValueParameterSwiftModel : KotlinValueParameterSwiftModel {

    override var argumentLabel: String

    override var flowMappingStrategy: FlowMappingStrategy
}
