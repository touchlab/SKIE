package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

class OriginalKotlinClassSwiftModel(
    private val delegate: KotlinClassSwiftModel,
    containingType: Lazy<KotlinClassSwiftModel?>,
) : KotlinClassSwiftModel by delegate {

    override val identifier: String = delegate.identifier

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val containingType: KotlinClassSwiftModel? by containingType

    override val bridge: TypeSwiftModel? = null

    override val isChanged: Boolean = false
}
