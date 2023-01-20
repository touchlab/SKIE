package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

class OriginalKotlinFileSwiftModel(
    delegate: KotlinTypeSwiftModel,
) : KotlinTypeSwiftModel by delegate {

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val containingType: KotlinClassSwiftModel? = null

    override val identifier: String = delegate.identifier

    override val bridge: TypeSwiftModel? = null

    override val isChanged: Boolean = false
}
