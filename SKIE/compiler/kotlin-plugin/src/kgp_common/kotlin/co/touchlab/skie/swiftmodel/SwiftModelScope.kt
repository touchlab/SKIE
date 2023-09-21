package co.touchlab.skie.swiftmodel

import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridge
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridgeParameter
import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface SwiftModelScope {

    val sirProvider: SirProvider

    val sirBuiltins: SirBuiltins
        get() = sirProvider.sirBuiltins

    val exposedClasses: List<KotlinClassSwiftModel>

    val exposedFiles: List<KotlinTypeSwiftModel>

    val exposedTypes: List<KotlinTypeSwiftModel>
        get() = exposedClasses + exposedFiles

    val allExposedMembers: List<KotlinCallableMemberSwiftModel>

    fun referenceClass(classFqName: String): KotlinClassSwiftModel

    val CallableMemberDescriptor.swiftModel: KotlinCallableMemberSwiftModel

    val FunctionDescriptor.swiftModel: KotlinFunctionSwiftModel

    val FunctionDescriptor.asyncSwiftModel: KotlinFunctionSwiftModel

    val ParameterDescriptor.swiftModel: KotlinValueParameterSwiftModel

    val PropertyDescriptor.swiftModel: KotlinPropertySwiftModel

    val PropertyDescriptor.regularPropertySwiftModel: KotlinRegularPropertySwiftModel

    val PropertyDescriptor.convertedPropertySwiftModel: KotlinConvertedPropertySwiftModel

    val ClassDescriptor.hasSwiftModel: Boolean

    val ClassDescriptor.swiftModel: KotlinClassSwiftModel

    val ClassDescriptor.swiftModelOrNull: KotlinClassSwiftModel?

    val ClassDescriptor.enumEntrySwiftModel: KotlinEnumEntrySwiftModel

    val SirClass.swiftModelOrNull: KotlinTypeSwiftModel?

    val SourceFile.swiftModel: KotlinTypeSwiftModel

    fun CallableMemberDescriptor.owner(): KotlinTypeSwiftModel?

    fun CallableMemberDescriptor.receiverType(): SirType

    fun ClassDescriptor.receiverType(): SirType

    fun PropertyDescriptor.propertyType(
        baseDescriptor: PropertyDescriptor,
        genericExportScope: SwiftGenericExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType

    fun FunctionDescriptor.returnType(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridge.ReturnValue,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType

    fun FunctionDescriptor.asyncReturnType(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridgeParameter.ValueParameter.SuspendCompletion,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType

    fun FunctionDescriptor.getParameterType(
        descriptor: ParameterDescriptor?,
        bridge: MethodBridgeParameter.ValueParameter,
        genericExportScope: SwiftGenericExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType
}

fun SwiftModelScope.getSwiftModel(callableMemberDescriptor: CallableMemberDescriptor): KotlinCallableMemberSwiftModel =
    callableMemberDescriptor.swiftModel

fun SwiftModelScope.getSwiftModel(functionDescriptor: FunctionDescriptor): KotlinFunctionSwiftModel =
    functionDescriptor.swiftModel

fun SwiftModelScope.getSwiftModel(parameterDescriptor: ParameterDescriptor): KotlinValueParameterSwiftModel =
    parameterDescriptor.swiftModel

fun SwiftModelScope.getSwiftModel(propertyDescriptor: PropertyDescriptor): KotlinPropertySwiftModel =
    propertyDescriptor.swiftModel

fun SwiftModelScope.getSwiftModel(classDescriptor: ClassDescriptor): KotlinClassSwiftModel =
    classDescriptor.swiftModel

fun SwiftModelScope.getSwiftModel(sourceFile: SourceFile): KotlinTypeSwiftModel =
    sourceFile.swiftModel

fun SwiftModelScope.getSwiftModelOrNull(classDescriptor: ClassDescriptor): KotlinClassSwiftModel? =
    classDescriptor.swiftModelOrNull
