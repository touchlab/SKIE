package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridge
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftTypeModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface SwiftModelScope {

    val exposedClasses: List<KotlinClassSwiftModel>

    val exposedFiles: List<KotlinTypeSwiftModel>

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

    fun CallableMemberDescriptor.receiverTypeModel(): TypeSwiftModel

    fun PropertyDescriptor.propertyTypeModel(genericExportScope: SwiftGenericExportScope): SwiftTypeModel

    fun FunctionDescriptor.returnTypeModel(genericExportScope: SwiftGenericExportScope, bridge: MethodBridge.ReturnValue): SwiftTypeModel

    fun FunctionDescriptor.asyncReturnTypeModel(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridgeParameter.ValueParameter.SuspendCompletion,
    ): SwiftTypeModel

    fun FunctionDescriptor.getParameterType(
        descriptor: ParameterDescriptor?,
        bridge: MethodBridgeParameter.ValueParameter,
        genericExportScope: SwiftGenericExportScope,
    ): SwiftTypeModel
}
