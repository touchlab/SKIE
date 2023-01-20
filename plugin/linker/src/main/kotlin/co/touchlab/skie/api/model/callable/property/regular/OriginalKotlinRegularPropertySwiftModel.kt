package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel

class OriginalKotlinRegularPropertySwiftModel(
    delegate: KotlinRegularPropertySwiftModel,
) : KotlinRegularPropertySwiftModel by delegate {

    override var visibility: SwiftModelVisibility = delegate.visibility

    override var identifier: String = delegate.identifier

    override val isChanged: Boolean = false
}
