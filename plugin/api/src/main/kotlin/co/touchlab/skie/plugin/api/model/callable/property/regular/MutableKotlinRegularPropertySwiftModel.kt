package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy

interface MutableKotlinRegularPropertySwiftModel : KotlinRegularPropertySwiftModel, MutableKotlinPropertySwiftModel,
    MutableKotlinDirectlyCallableMemberSwiftModel {

    override var flowMappingStrategy: FlowMappingStrategy
}
