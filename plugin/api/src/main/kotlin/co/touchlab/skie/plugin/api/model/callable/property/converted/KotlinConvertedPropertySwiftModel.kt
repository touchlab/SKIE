package co.touchlab.skie.plugin.api.model.callable.property.converted

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel

interface KotlinConvertedPropertySwiftModel : KotlinPropertySwiftModel {

    val original: KotlinConvertedPropertySwiftModel

    val getter: KotlinFunctionSwiftModel

    val setter: KotlinFunctionSwiftModel?

    val accessors: List<KotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
