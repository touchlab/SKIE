package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface SwiftModelScope {

    val FunctionDescriptor.swiftModel: KotlinFunctionSwiftModel

    val PropertyDescriptor.swiftModel: KotlinPropertySwiftModel

    val ClassDescriptor.swiftModel: KotlinTypeSwiftModel

    val SourceFile.swiftModel: KotlinTypeSwiftModel
}
