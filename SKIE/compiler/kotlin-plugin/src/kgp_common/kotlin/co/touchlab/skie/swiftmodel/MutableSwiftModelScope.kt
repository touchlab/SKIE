package co.touchlab.skie.swiftmodel

import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.swiftmodel.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.MutableKotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface MutableSwiftModelScope : SwiftModelScope {

    override val exposedClasses: List<MutableKotlinClassSwiftModel>

    override val exposedFiles: List<MutableKotlinTypeSwiftModel>

    override val exposedTypes: List<MutableKotlinTypeSwiftModel>
        get() = exposedClasses + exposedFiles

    override val allExposedMembers: List<MutableKotlinCallableMemberSwiftModel>

    override fun referenceClass(classFqName: String): MutableKotlinClassSwiftModel

    override val CallableMemberDescriptor.swiftModel: MutableKotlinCallableMemberSwiftModel

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel

    override val ParameterDescriptor.swiftModel: MutableKotlinValueParameterSwiftModel

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel

    override val PropertyDescriptor.convertedPropertySwiftModel: MutableKotlinConvertedPropertySwiftModel

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel

    override val ClassDescriptor.swiftModelOrNull: MutableKotlinClassSwiftModel?

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
}

fun MutableSwiftModelScope.getSwiftModel(callableMemberDescriptor: CallableMemberDescriptor): MutableKotlinCallableMemberSwiftModel =
    callableMemberDescriptor.swiftModel

fun MutableSwiftModelScope.getSwiftModel(functionDescriptor: FunctionDescriptor): MutableKotlinFunctionSwiftModel =
    functionDescriptor.swiftModel

fun MutableSwiftModelScope.getSwiftModel(parameterDescriptor: ParameterDescriptor): MutableKotlinValueParameterSwiftModel =
    parameterDescriptor.swiftModel

fun MutableSwiftModelScope.getSwiftModel(propertyDescriptor: PropertyDescriptor): MutableKotlinPropertySwiftModel =
    propertyDescriptor.swiftModel

fun MutableSwiftModelScope.getSwiftModel(classDescriptor: ClassDescriptor): MutableKotlinClassSwiftModel =
    classDescriptor.swiftModel

fun MutableSwiftModelScope.getSwiftModel(sourceFile: SourceFile): MutableKotlinTypeSwiftModel =
    sourceFile.swiftModel

fun MutableSwiftModelScope.getSwiftModelOrNull(classDescriptor: ClassDescriptor): MutableKotlinClassSwiftModel? =
    classDescriptor.swiftModelOrNull
