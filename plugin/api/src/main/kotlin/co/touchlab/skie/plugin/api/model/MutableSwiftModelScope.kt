package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.MutableKotlinInterfaceExtensionPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

interface MutableSwiftModelScope : SwiftModelScope {

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel

    override val ValueParameterDescriptor.swiftModel: MutableKotlinParameterSwiftModel

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel

    override val PropertyDescriptor.interfaceExtensionPropertySwiftModel: MutableKotlinInterfaceExtensionPropertySwiftModel

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
}
