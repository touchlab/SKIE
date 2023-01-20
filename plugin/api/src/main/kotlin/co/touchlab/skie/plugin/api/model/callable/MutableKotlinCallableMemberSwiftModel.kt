package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel

interface MutableKotlinCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val receiver: MutableKotlinTypeSwiftModel

    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>

    fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT
}
