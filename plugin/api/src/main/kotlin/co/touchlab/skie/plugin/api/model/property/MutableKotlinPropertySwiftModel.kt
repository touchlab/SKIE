package co.touchlab.skie.plugin.api.model.property

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel

interface MutableKotlinPropertySwiftModel : KotlinPropertySwiftModel {

    override var visibility: SwiftModelVisibility

    override val receiver: MutableKotlinTypeSwiftModel

    override var identifier: String
}
