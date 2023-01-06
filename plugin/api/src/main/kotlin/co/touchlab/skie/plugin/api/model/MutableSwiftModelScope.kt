package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface MutableSwiftModelScope : SwiftModelScope {

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel

    override val ClassDescriptor.swiftModel: MutableKotlinTypeSwiftModel

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
}
