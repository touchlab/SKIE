package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility

interface MutableKotlinDirectlyCallableMemberSwiftModel : KotlinDirectlyCallableMemberSwiftModel, MutableKotlinCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    override var visibility: SwiftModelVisibility

    override var identifier: String

    override var collisionResolutionStrategy: KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy

    fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT
}
