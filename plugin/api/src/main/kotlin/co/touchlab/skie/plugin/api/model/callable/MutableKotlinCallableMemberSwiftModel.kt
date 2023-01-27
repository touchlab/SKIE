package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

interface MutableKotlinCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val receiver: TypeSwiftModel

    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>

    fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT
}
