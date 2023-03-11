package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy

internal class OriginalKotlinFunctionSwiftModel(
    private val delegate: KotlinFunctionSwiftModelWithCore,
) : KotlinFunctionSwiftModel by delegate {

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val identifier: String = delegate.identifier

    override val collisionResolutionStrategy: CollisionResolutionStrategy = delegate.collisionResolutionStrategy

    override val returnTypeFlowMappingStrategy: FlowMappingStrategy = delegate.returnTypeFlowMappingStrategy

    // override val isChanged: Boolean = false

    override val reference: String
        get() = delegate.core.reference(this)

    override val name: String
        get() = delegate.core.name(this)

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
