package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel

interface KotlinDirectlyCallableMemberSwiftModelVisitor<OUT> {

    fun visit(function: KotlinFunctionSwiftModel): OUT

    fun visit(regularProperty: KotlinRegularPropertySwiftModel): OUT

    interface Unit : KotlinDirectlyCallableMemberSwiftModelVisitor<kotlin.Unit> {

        override fun visit(function: KotlinFunctionSwiftModel) {
        }

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
        }
    }
}

