package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.sir.type.SirType

interface MutableKotlinCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val receiver: SirType

    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT
}
