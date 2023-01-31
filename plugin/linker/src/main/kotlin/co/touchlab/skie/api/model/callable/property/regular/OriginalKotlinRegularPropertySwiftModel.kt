package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
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

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
