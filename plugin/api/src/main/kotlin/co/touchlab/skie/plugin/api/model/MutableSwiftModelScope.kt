package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface MutableSwiftModelScope : SwiftModelScope {

    override val CallableMemberDescriptor.swiftModel: MutableKotlinCallableMemberSwiftModel

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel

    override val ParameterDescriptor.swiftModel: MutableKotlinParameterSwiftModel

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel

    override val PropertyDescriptor.convertedPropertySwiftModel: MutableKotlinConvertedPropertySwiftModel

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
}
