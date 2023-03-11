package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.translation.SirType

interface MutableKotlinCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val receiver: SirType

    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT
}
