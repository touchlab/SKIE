package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel

interface MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT> {

    fun visit(function: MutableKotlinFunctionSwiftModel): OUT

    fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel): OUT

    interface Unit : MutableKotlinDirectlyCallableMemberSwiftModelVisitor<kotlin.Unit> {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
        }
    }
}

