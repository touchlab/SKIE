package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridge
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.api.model.type.translation.SirType
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface SwiftModelScope {

    val exposedClasses: List<KotlinClassSwiftModel>

    val exposedFiles: List<KotlinTypeSwiftModel>

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

    val SourceFile.swiftModel: KotlinTypeSwiftModel

    fun CallableMemberDescriptor.owner(): SwiftIrExtensibleDeclaration

    fun CallableMemberDescriptor.receiverType(): SirType

    fun PropertyDescriptor.propertyType(
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
