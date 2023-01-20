package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel

interface KotlinCallableMemberSwiftModelVisitor<OUT> {

    fun visit(function: KotlinFunctionSwiftModel): OUT

    fun visit(regularProperty: KotlinRegularPropertySwiftModel): OUT

    fun visit(convertedProperty: KotlinConvertedPropertySwiftModel): OUT

    interface Unit : KotlinCallableMemberSwiftModelVisitor<kotlin.Unit> {

        override fun visit(function: KotlinFunctionSwiftModel) {
        }

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel) {
        }

        override fun visit(convertedProperty: KotlinConvertedPropertySwiftModel) {
            convertedProperty.accessors.forEach {
                visit(it)
            }
        }
    }
}

