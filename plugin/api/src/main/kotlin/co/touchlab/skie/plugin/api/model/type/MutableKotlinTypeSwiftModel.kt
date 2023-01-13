package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility

interface MutableKotlinTypeSwiftModel : KotlinTypeSwiftModel {

    override var visibility: SwiftModelVisibility

    override var containingType: MutableKotlinClassSwiftModel?

    override var identifier: String

    override var bridge: TypeSwiftModel?
}
