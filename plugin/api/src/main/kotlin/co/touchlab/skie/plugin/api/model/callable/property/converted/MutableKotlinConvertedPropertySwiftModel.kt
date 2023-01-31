package co.touchlab.skie.plugin.api.model.callable.property.converted

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.MutableKotlinPropertySwiftModel

interface MutableKotlinConvertedPropertySwiftModel : KotlinConvertedPropertySwiftModel, MutableKotlinPropertySwiftModel {

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = accessors

    override val getter: MutableKotlinFunctionSwiftModel

    override val setter: MutableKotlinFunctionSwiftModel?

    override val accessors: List<MutableKotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
