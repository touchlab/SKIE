package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel

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

