package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel

interface MutableKotlinRegularPropertySwiftModel : KotlinRegularPropertySwiftModel {

    override var visibility: SwiftModelVisibility

    override val receiver: MutableKotlinTypeSwiftModel

    override var identifier: String
}
