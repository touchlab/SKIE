package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel

class OriginalKotlinRegularPropertySwiftModel(
    private val delegate: KotlinRegularPropertySwiftModel,
) : KotlinRegularPropertySwiftModel by delegate {

    override var visibility: SwiftModelVisibility = delegate.visibility

    override var identifier: String = delegate.identifier

    override val collisionResolutionStrategy: CollisionResolutionStrategy = delegate.collisionResolutionStrategy

    override val isChanged: Boolean = false

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = delegate.directlyCallableMembers
}
