package co.touchlab.skie.swiftmodel

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModelWithCore
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.factory.SwiftModelFactory
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.swiftmodel.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridge
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridgeParameter
import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
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
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class SwiftModelProvider(
    override val sirProvider: SirProvider,
    namer: ObjCExportNamer,
    private val descriptorProvider: DescriptorProvider,
    private val bridgeProvider: DescriptorBridgeProvider,
) : MutableSwiftModelScope {

    private val swiftModelFactory = SwiftModelFactory(this, descriptorProvider, namer, bridgeProvider, sirProvider)

    private val translator = sirProvider.translator

    private val members = swiftModelFactory.createMembers(descriptorProvider.allExposedMembers)

    private val functionSwiftModels = members.filterIsInstance<FunctionDescriptor, KotlinFunctionSwiftModelWithCore>()
    private val asyncFunctionSwiftModels = swiftModelFactory.createAsyncFunctions(functionSwiftModels.values)
    private val regularPropertySwiftModels = members.filterIsInstance<PropertyDescriptor, MutableKotlinRegularPropertySwiftModel>()
    private val convertedPropertySwiftModels = members.filterIsInstance<PropertyDescriptor, MutableKotlinConvertedPropertySwiftModel>()

    private val parameterSwiftModels = (functionSwiftModels.values + convertedPropertySwiftModels.flatMap { it.value.accessors })
        .flatMap { it.valueParameters }
        .mapNotNull { swiftModel -> swiftModel.descriptor?.let { it to swiftModel } }
        .toMap()

    private val classSwiftModels = swiftModelFactory.createClasses(descriptorProvider.exposedClasses)
    private val enumEntrySwiftModels = swiftModelFactory.createEnumEntries(descriptorProvider.exposedClasses)
    private val fileSwiftModels = swiftModelFactory.createFiles(descriptorProvider.exposedFiles)

    private val classByFqNameCache = mutableMapOf<String, MutableKotlinClassSwiftModel>()

    private val sirTypeDeclarationsToSwiftModelCache =
        (classSwiftModels + fileSwiftModels).values.associateBy { it.kotlinSirClass }.toMutableMap()

    init {
        sirProvider.finishInitialization()
    }

    override fun referenceClass(classFqName: String): MutableKotlinClassSwiftModel =
        classByFqNameCache.getOrPut(classFqName) {
            classSwiftModels.values.single { it.classDescriptor.fqNameSafe == FqName(classFqName) }
        }

    override val exposedClasses: List<MutableKotlinClassSwiftModel> =
        descriptorProvider.exposedClasses.map { it.swiftModel }

    override val exposedFiles: List<MutableKotlinTypeSwiftModel> =
        descriptorProvider.exposedFiles.map { it.swiftModel }

    override val allExposedMembers: List<MutableKotlinCallableMemberSwiftModel> =
        descriptorProvider.allExposedMembers.map { it.swiftModel }

    override val CallableMemberDescriptor.swiftModel: MutableKotlinCallableMemberSwiftModel
        get() = functionSwiftModels[this.original]
            ?: regularPropertySwiftModels[this.original]
            ?: convertedPropertySwiftModels[this.original]
            ?: throwUnknownDescriptor()

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel
        get() = functionSwiftModels[this.original] ?: throwUnknownDescriptor()

    override val FunctionDescriptor.asyncSwiftModel: KotlinFunctionSwiftModel
        get() = asyncFunctionSwiftModels[this.original] ?: throwUnknownDescriptor()

    override val ParameterDescriptor.swiftModel: MutableKotlinValueParameterSwiftModel
        get() = parameterSwiftModels[this] ?: throwUnknownDescriptor()

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel
        get() = regularPropertySwiftModels[this.original]
            ?: convertedPropertySwiftModels[this.original]
            ?: throwUnknownDescriptor()

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel
        get() = regularPropertySwiftModels[this.original] ?: throwUnknownDescriptor()

    override val PropertyDescriptor.convertedPropertySwiftModel: MutableKotlinConvertedPropertySwiftModel
        get() = convertedPropertySwiftModels[this.original] ?: throwUnknownDescriptor()

    override val ClassDescriptor.hasSwiftModel: Boolean
        get() = classSwiftModels.containsKey(this.original)

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel
        get() = classSwiftModels[this.original] ?: throwUnknownDescriptor()

    override val ClassDescriptor.swiftModelOrNull: MutableKotlinClassSwiftModel?
        get() = classSwiftModels[this.original]

    override val SirClass.swiftModelOrNull: KotlinTypeSwiftModel?
        get() = sirTypeDeclarationsToSwiftModelCache[this]

    override val ClassDescriptor.enumEntrySwiftModel: KotlinEnumEntrySwiftModel
        get() = enumEntrySwiftModels[this.original] ?: throwUnknownDescriptor()

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
        get() = fileSwiftModels[this]
            ?: throw IllegalArgumentException("File $this is not exposed and therefore does not have a SwiftModel.")

    override fun CallableMemberDescriptor.owner(): KotlinTypeSwiftModel? {
        val receiverClassDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(this)
        val containingDeclaration = containingDeclaration

        return when {
            receiverClassDescriptor != null -> receiverClassDescriptor.swiftModelOrNull
            this is PropertyAccessorDescriptor -> correspondingProperty.swiftModel.owner
            containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
            else -> error("Unsupported containing declaration for $this")
        }
    }

    override fun CallableMemberDescriptor.receiverType(): SirType {
        val receiverClassDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(this)
        val containingDeclaration = containingDeclaration

        return when {
            receiverClassDescriptor != null -> receiverClassDescriptor.receiverType()
            this is PropertyAccessorDescriptor -> correspondingProperty.swiftModel.receiver
            containingDeclaration is PackageFragmentDescriptor -> translator.mapFileType(this.findSourceFile())
            else -> error("Unsupported containing declaration for $this")
        }
    }

    override fun ClassDescriptor.receiverType(): SirType =
        translator.mapReferenceType(
            this.defaultType,
            // TODO ?: SwiftGenericExportScope.None is a hack that relies on the fact that none of the types with SwiftModel can inherit from a special type that is generic
            SwiftExportScope(this.swiftModelOrNull?.swiftGenericExportScope ?: SwiftGenericExportScope.None, SwiftExportScope.Flags.ReferenceType),
            FlowMappingStrategy.TypeArgumentsOnly,
        )

    override fun PropertyDescriptor.propertyType(
        baseDescriptor: PropertyDescriptor,
        genericExportScope: SwiftGenericExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        val getterBridge = bridgeProvider.bridgeMethod(baseDescriptor.getter!!)
        val exportScope = SwiftExportScope(genericExportScope)
        return translator.mapReturnType(getterBridge.returnBridge, getter!!, exportScope, flowMappingStrategy)
    }

    override fun FunctionDescriptor.returnType(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridge.ReturnValue,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        val exportScope = SwiftExportScope(genericExportScope)
        return translator.mapReturnType(bridge, this, exportScope, flowMappingStrategy)
    }

    override fun FunctionDescriptor.asyncReturnType(
        genericExportScope: SwiftGenericExportScope,
        bridge: MethodBridgeParameter.ValueParameter.SuspendCompletion,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        val exportScope = SwiftExportScope(genericExportScope)
        return if (bridge.useUnitCompletion) {
            sirBuiltins.Swift.Void.defaultType
        } else {
            translator.mapReferenceType(returnType!!, exportScope, flowMappingStrategy)
        }
    }

    override fun FunctionDescriptor.getParameterType(
        descriptor: ParameterDescriptor?,
        bridge: MethodBridgeParameter.ValueParameter,
        genericExportScope: SwiftGenericExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        val exportScope = SwiftExportScope(genericExportScope, SwiftExportScope.Flags.Escaping)
        return when (bridge) {
            is MethodBridgeParameter.ValueParameter.Mapped -> translator.mapType(
                descriptor!!.type,
                exportScope,
                bridge.bridge,
                flowMappingStrategy,
            )
            MethodBridgeParameter.ValueParameter.ErrorOutParameter -> NullableSirType(sirBuiltins.Swift.UnsafeMutableRawPointer.defaultType)
            is MethodBridgeParameter.ValueParameter.SuspendCompletion -> {
                val resultType = if (bridge.useUnitCompletion) {
                    null
                } else {
                    translator.mapReferenceType(
                        returnType!!,
                        exportScope.removingFlags(SwiftExportScope.Flags.Escaping),
                        flowMappingStrategy,
                    )
                        .toNonNull()
                        .let { NullableSirType(it) }
                }
                LambdaSirType(
                    returnType = sirBuiltins.Swift.Void.defaultType,
                    valueParameterTypes = listOfNotNull(
                        resultType,
                        NullableSirType(sirBuiltins.Swift.Error.defaultType),
                    ),
                    isEscaping = true,
                )
            }
        }
    }

    private fun DeclarationDescriptor.throwUnknownDescriptor(): Nothing {
        throw IllegalArgumentException(
            "Cannot find SwiftModel for descriptor: $this. Possible reasons: " +
                "Descriptor is not exposed and therefore does not have a SwiftModel. " +
                "Or it is exposed but as another type (for example as ConvertedProperty instead of a RegularProperty).",
        )
    }

    private inline fun <reified K2, reified V2> Map<*, *>.filterIsInstance(): Map<K2, V2> =
        this.filterKeys { it is K2 }
            .mapKeys { it.key as K2 }
            .filterValues { it is V2 }
            .mapValues { it.value as V2 }
}
