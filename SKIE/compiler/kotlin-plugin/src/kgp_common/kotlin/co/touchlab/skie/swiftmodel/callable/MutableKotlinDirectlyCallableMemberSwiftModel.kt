package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.SwiftModelVisibility

interface MutableKotlinDirectlyCallableMemberSwiftModel : KotlinDirectlyCallableMemberSwiftModel, MutableKotlinCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    override var visibility: SwiftModelVisibility

    override var identifier: String

    override var collisionResolutionStrategy: KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy

    fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT
}
