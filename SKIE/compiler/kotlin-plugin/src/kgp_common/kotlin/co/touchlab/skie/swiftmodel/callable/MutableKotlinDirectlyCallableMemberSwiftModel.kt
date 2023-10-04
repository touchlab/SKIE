package co.touchlab.skie.swiftmodel.callable

interface MutableKotlinDirectlyCallableMemberSwiftModel : KotlinDirectlyCallableMemberSwiftModel, MutableKotlinCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    override var collisionResolutionStrategy: KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy

    fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT
}
