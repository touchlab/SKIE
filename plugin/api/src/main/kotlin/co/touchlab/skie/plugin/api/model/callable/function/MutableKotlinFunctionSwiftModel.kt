package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel, MutableKotlinDirectlyCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>

    override val parameters: List<MutableKotlinParameterSwiftModel>

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
