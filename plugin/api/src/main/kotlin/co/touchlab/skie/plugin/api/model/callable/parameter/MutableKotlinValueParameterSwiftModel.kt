package co.touchlab.skie.plugin.api.model.callable.parameter

interface MutableKotlinValueParameterSwiftModel : KotlinValueParameterSwiftModel {

    override var argumentLabel: String

    override var isFlowMappingEnabled: Boolean
}
