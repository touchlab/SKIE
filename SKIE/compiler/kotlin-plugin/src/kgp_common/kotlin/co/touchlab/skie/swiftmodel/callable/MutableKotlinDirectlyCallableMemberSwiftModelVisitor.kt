package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel

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

