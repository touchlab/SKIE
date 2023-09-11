package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel

interface MutableKotlinCallableMemberSwiftModelVisitor<OUT> {

    fun visit(function: MutableKotlinFunctionSwiftModel): OUT

    fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel): OUT

    fun visit(convertedProperty: MutableKotlinConvertedPropertySwiftModel): OUT

    interface Unit : MutableKotlinCallableMemberSwiftModelVisitor<kotlin.Unit> {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
        }

        override fun visit(convertedProperty: MutableKotlinConvertedPropertySwiftModel) {
            convertedProperty.accessors.forEach {
                visit(it)
            }
        }
    }
}

