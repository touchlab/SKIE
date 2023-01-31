package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel

class OriginalKotlinFunctionSwiftModel(
    delegate: KotlinFunctionSwiftModel,
) : KotlinFunctionSwiftModel by delegate {

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val identifier: String = delegate.identifier

    override val collisionResolutionStrategy: CollisionResolutionStrategy = delegate.collisionResolutionStrategy

    override val isChanged: Boolean = false
}
