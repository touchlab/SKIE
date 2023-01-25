package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel, MutableKotlinCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>

    override var visibility: SwiftModelVisibility

    override var identifier: String

    override val parameters: List<MutableKotlinParameterSwiftModel>

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
