package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy

interface MutableKotlinRegularPropertySwiftModel : KotlinRegularPropertySwiftModel, MutableKotlinPropertySwiftModel,
    MutableKotlinDirectlyCallableMemberSwiftModel {

    override var flowMappingStrategy: FlowMappingStrategy
}
