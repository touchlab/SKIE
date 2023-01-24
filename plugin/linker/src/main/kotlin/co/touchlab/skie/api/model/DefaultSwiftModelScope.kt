package co.touchlab.skie.api.model

import co.touchlab.skie.api.model.factory.SwiftModelFactory
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftLambdaTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNullableRefefenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPointerTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftVoidTypeModel
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType

class DefaultSwiftModelScope(
    private val namer: ObjCExportNamer,
    private val descriptorProvider: DescriptorProvider,
    private val bridgeProvider: DescriptorBridgeProvider,
    private var translator: SwiftTypeTranslator,
) : MutableSwiftModelScope {

    private val swiftModelFactory = SwiftModelFactory(this, descriptorProvider, namer, bridgeProvider)

    private val members = swiftModelFactory.createMembers(descriptorProvider.allExposedMembers)

    private val functionSwiftModels = members.filterIsInstance<FunctionDescriptor, MutableKotlinFunctionSwiftModel>()
    private val regularPropertySwiftModels = members.filterIsInstance<PropertyDescriptor, MutableKotlinRegularPropertySwiftModel>()
    private val convertedPropertySwiftModels = members.filterIsInstance<PropertyDescriptor, MutableKotlinConvertedPropertySwiftModel>()

    private val valueParameterSwiftModels = (functionSwiftModels.values + convertedPropertySwiftModels.flatMap { it.value.accessors })
        .flatMap { it.parameters }
        .mapNotNull { swiftModel -> (swiftModel.descriptor as? ValueParameterDescriptor)?.let { it to swiftModel } }
        .toMap()

    private val classSwiftModels = swiftModelFactory.createClasses(descriptorProvider.transitivelyExposedClasses)
    private val enumEntrySwiftModels = swiftModelFactory.createEnumEntries(descriptorProvider.transitivelyExposedClasses)
    private val fileSwiftModels = swiftModelFactory.createFiles(descriptorProvider.exposedFiles)

    override val CallableMemberDescriptor.swiftModel: MutableKotlinCallableMemberSwiftModel
        get() = functionSwiftModels[this]
            ?: regularPropertySwiftModels[this]
            ?: convertedPropertySwiftModels[this]
            ?: throwUnknownDescriptor()

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel
        get() = functionSwiftModels[this] ?: throwUnknownDescriptor()

    override val ValueParameterDescriptor.swiftModel: MutableKotlinParameterSwiftModel
        get() = valueParameterSwiftModels[this] ?: throwUnknownDescriptor()

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel
        get() = regularPropertySwiftModels[this]
            ?: convertedPropertySwiftModels[this]
            ?: throwUnknownDescriptor()

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel
        get() = regularPropertySwiftModels[this] ?: throwUnknownDescriptor()

    override val PropertyDescriptor.convertedPropertySwiftModel: MutableKotlinConvertedPropertySwiftModel
        get() = convertedPropertySwiftModels[this] ?: throwUnknownDescriptor()

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel
        get() = classSwiftModels[this] ?: throwUnknownDescriptor()

    override val ClassDescriptor.enumEntrySwiftModel: KotlinEnumEntrySwiftModel
        get() = enumEntrySwiftModels[this] ?: throwUnknownDescriptor()

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
        get() = fileSwiftModels[this]
            ?: throw IllegalArgumentException("File $this is not exposed and therefore does not have a SwiftModel.")

    override val KotlinType.isBridged: Boolean
        get() = TODO("Not yet implemented")

    override fun CallableMemberDescriptor.receiverTypeModel(): TypeSwiftModel {
        val categoryClass = descriptorProvider.getClassIfCategory(this)
        val containingDeclaration = containingDeclaration

        val exportScope = SwiftExportScope(SwiftGenericExportScope.None, SwiftExportScope.Flags.ReferenceType)
        return when {
            categoryClass != null -> translator.mapReferenceType(categoryClass.defaultType, exportScope)
            this is PropertyAccessorDescriptor -> correspondingProperty.swiftModel.receiver
            containingDeclaration is ClassDescriptor -> translator.mapReferenceType(containingDeclaration.defaultType, exportScope)
            containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
            else -> error("Unsupported containing declaration for $this")
        }
    }

    override fun PropertyDescriptor.propertyTypeModel(genericExportScope: SwiftGenericExportScope): SwiftTypeModel {
        val getterBridge = bridgeProvider.bridgeMethod(getter!!)
        val exportScope = SwiftExportScope(genericExportScope)
        return translator.mapReturnType(getterBridge.returnBridge, getter!!, exportScope)
    }

    override fun FunctionDescriptor.returnTypeModel(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridge.ReturnValue,
    ): SwiftTypeModel {
        val exportScope = SwiftExportScope(genericExportScope)
        return translator.mapReturnType(bridge, this, exportScope)
    }

    override fun FunctionDescriptor.getParameterType(
        descriptor: ParameterDescriptor?,
        bridge: MethodBridgeParameter.ValueParameter,
        genericExportScope: SwiftGenericExportScope
    ): SwiftTypeModel {
        val exportScope = SwiftExportScope(genericExportScope, SwiftExportScope.Flags.Escaping)
        return when (bridge) {
            is MethodBridgeParameter.ValueParameter.Mapped -> translator.mapType(descriptor!!.type, exportScope, bridge.bridge)
            MethodBridgeParameter.ValueParameter.ErrorOutParameter ->
                SwiftPointerTypeModel(SwiftNullableRefefenceTypeModel(SwiftClassTypeModel("Error")), nullable = true)
            is MethodBridgeParameter.ValueParameter.SuspendCompletion -> {
                val resultType = if (bridge.useUnitCompletion) {
                    null
                } else {
                    when (val it = translator.mapReferenceType(returnType!!, exportScope.removingFlags(SwiftExportScope.Flags.Escaping))) {
                        is SwiftNonNullReferenceTypeModel -> SwiftNullableRefefenceTypeModel(it, isNullableResult = false)
                        is SwiftNullableRefefenceTypeModel -> SwiftNullableRefefenceTypeModel(it.nonNullType, isNullableResult = true)
                    }
                }
                SwiftLambdaTypeModel(
                    returnType = SwiftVoidTypeModel,
                    parameterTypes = listOfNotNull(
                        resultType,
                        SwiftNullableRefefenceTypeModel(SwiftClassTypeModel("Error"))
                    ),
                    isEscaping = true
                )
            }
        }
    }

    private fun DeclarationDescriptor.throwUnknownDescriptor(): Nothing {
        throw IllegalArgumentException(
            "Cannot find SwiftModel for descriptor: $this. Possible reasons: " +
            "Descriptor is not exposed and therefore does not have a SwiftModel. " +
            "Or it is exposed but as another type (for example as ConvertedProperty instead of a RegularProperty)."
        )
    }

    private inline fun <reified K2, reified V2> Map<*, *>.filterIsInstance(): Map<K2, V2> =
        this.filterKeys { it is K2 }
            .mapKeys { it.key as K2 }
            .filterValues { it is V2 }
            .mapValues { it.value as V2 }
}
