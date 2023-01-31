package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel

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

