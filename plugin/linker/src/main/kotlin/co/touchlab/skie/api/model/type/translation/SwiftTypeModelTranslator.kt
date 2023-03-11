@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridge
import co.touchlab.skie.plugin.api.model.type.bridge.NativeTypeBridge
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import co.touchlab.skie.plugin.api.model.type.translation.ObjcProtocolSirType
import co.touchlab.skie.plugin.api.model.type.translation.SirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyObjectSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnySirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftErrorSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftInstanceSirType
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.model.type.translation.SwiftLambdaSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftMetaClassSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNullableReferenceSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPointerSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPrimitiveSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftProtocolSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftReferenceSirType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftVoidSirType
import co.touchlab.skie.plugin.api.sir.declaration.isHashable
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isInlined
import org.jetbrains.kotlin.backend.konan.isKotlinObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes

fun SirType.makeNullableIfReferenceOrPointer(): SirType = when (this) {
    is SwiftPointerSirType -> SwiftPointerSirType(pointee, nullable = true)
    is SwiftNonNullReferenceSirType -> SwiftNullableReferenceSirType(this)
    is SwiftNullableReferenceSirType, is SwiftPrimitiveSirType, SwiftVoidSirType, SwiftErrorSirType -> this
}

internal tailrec fun KotlinType.getErasedTypeClass(): ClassDescriptor =
    TypeUtils.getClassDescriptor(this) ?: this.constructor.supertypes.first().getErasedTypeClass()

class SwiftTypeTranslator(
    private val descriptorProvider: DescriptorProvider,
    val namer: ObjCExportNamer,
    val problemCollector: SwiftTranslationProblemCollector,
    val builtinSwiftBridgeableProvider: BuiltinSwiftBridgeableProvider,
    val builtinKotlinDeclarations: BuiltinDeclarations.Kotlin,
    val swiftIrDeclarationRegistry: SwiftIrDeclarationRegistry,
) {

    context(SwiftModelScope)
    internal fun mapFileType(sourceFile: SourceFile): SirType {
        val fileModel = sourceFile.swiftModel
        return SwiftClassSirType(fileModel.swiftIrDeclaration)
    }

    context(SwiftModelScope)
    internal fun mapReturnType(
        returnBridge: MethodBridge.ReturnValue,
        method: FunctionDescriptor,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        return when (returnBridge) {
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void,
            -> SwiftVoidSirType
            MethodBridge.ReturnValue.HashCode -> SwiftPrimitiveSirType.NSUInteger
            is MethodBridge.ReturnValue.Mapped -> mapType(
                method.returnType!!,
                swiftExportScope,
                returnBridge.bridge,
                flowMappingStrategy,
            )
            MethodBridge.ReturnValue.WithError.Success -> SwiftVoidSirType
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(returnBridge.successBridge, method, swiftExportScope, flowMappingStrategy)

                if (!returnBridge.successMayBeZero) {
                    check(
                        successReturnType is SwiftNonNullReferenceSirType
                            || (successReturnType is SwiftPointerSirType && !successReturnType.nullable)
                    ) {
                        "Unexpected return type: $successReturnType in $method"
                    }
                }

                successReturnType.makeNullableIfReferenceOrPointer()
            }
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> SwiftInstanceSirType
        }
    }

    context(SwiftModelScope)
    internal fun mapReferenceType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftReferenceSirType =
        mapReferenceTypeIgnoringNullability(kotlinType, swiftExportScope, flowMappingStrategy).withNullabilityOf(kotlinType)

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullability(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftNonNullReferenceSirType {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: CustomTypeMapper)

        if (flowMappingStrategy == FlowMappingStrategy.Full) {
            val flowMapper = FlowTypeMappers.getMapperOrNull(kotlinType)
            if (flowMapper != null) {
                return flowMapper.mapType(kotlinType, this, swiftExportScope)
            }
        }

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                CustomTypeMappers.getMapper(descriptor)?.let { mapper ->
                    TypeMappingMatch(type, descriptor, mapper)
                }
            }
        }

        val mostSpecificMatches = typeMappingMatches.filter { match ->
            typeMappingMatches.all { otherMatch ->
                otherMatch.descriptor == match.descriptor || !otherMatch.descriptor.isSubclassOf(match.descriptor)
            }
        }

        if (mostSpecificMatches.size > 1) {
            val types = mostSpecificMatches.map { it.type }
            val firstType = types[0]
            val secondType = types[1]

            problemCollector.reportWarning(
                "Exposed type '$kotlinType' is '$firstType' and '$secondType' at the same time. " +
                    "This most likely wouldn't work as expected."
            )
        }

        mostSpecificMatches.firstOrNull()?.let {
            return it.mapper.mapType(it.type, this, swiftExportScope, flowMappingStrategy)
        }

        return mapReferenceTypeIgnoringNullabilitySkippingPredefined(kotlinType, swiftExportScope, flowMappingStrategy)
    }

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullabilitySkippingPredefined(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftNonNullReferenceSirType {
        if (kotlinType.isTypeParameter()) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> return SwiftAnyHashableSirType
                else -> {
                    val genericTypeUsage =
                        swiftExportScope.genericScope.getGenericTypeUsage(TypeUtils.getTypeParameterDescriptorOrNull(kotlinType))
                    if (genericTypeUsage != null) {
                        return genericTypeUsage
                    }
                }
            }
        }

        val classDescriptor = kotlinType.getErasedTypeClass()

        if (KotlinBuiltIns.isAny(classDescriptor) || classDescriptor.classId in CustomTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
            return idType(swiftExportScope)
        }

        if (classDescriptor.defaultType.isObjCObjectType()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(classDescriptor, swiftExportScope)
        }

        if (classDescriptor !in descriptorProvider.exposedClasses) {
            return idType(swiftExportScope)
        }

        return if (classDescriptor.kind.isInterface) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableSirType
                else -> {
                    translateClassOrInterfaceName(
                        descriptor = classDescriptor,
                        exportScope = swiftExportScope,
                        typeArgs = emptyList(),
                        ifKotlinType = { SwiftProtocolSirType(it.nonBridgedDeclaration as SwiftIrProtocolDeclaration) },
                    )
                }
            }
        } else {
            val typeParamScope = swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType)
            val typeArgs = kotlinType.arguments.map { typeProjection ->
                if (typeProjection.isStarProjection) {
                    idType(typeParamScope)
                } else {
                    mapReferenceTypeIgnoringNullability(typeProjection.type, typeParamScope, flowMappingStrategy.forGenerics())
                }
            }

            translateClassOrInterfaceName(
                descriptor = classDescriptor,
                exportScope = swiftExportScope,
                typeArgs = typeArgs,
                ifKotlinType = { SwiftClassSirType(it.nonBridgedDeclaration, typeArgs) },
            )
        }
    }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(
        descriptor: ClassDescriptor,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceSirType {
        if (descriptor.isObjCMetaClass()) return SwiftMetaClassSirType
        if (descriptor.isObjCProtocolClass()) return ObjcProtocolSirType

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            val bridge = builtinSwiftBridgeableProvider.bridgeFor(descriptor.fqNameSafe, swiftExportScope)
            return if (bridge != null) {
                bridge
            } else {
                val moduleName = "TODO: MODULE PLEASE"
                if (descriptor.kind.isInterface) {
                    TODO("Get from registry")
                    // val name = SwiftFqName.External(
                    //     module = moduleName,
                    //     name = descriptor.name.asString().removeSuffix("Protocol"),
                    // )
                    // SwiftProtocolSirType(
                    //     SwiftIrProtocolDeclaration(
                    //         name,
                    //         superTypes = listOf(BuiltinSwiftDeclarations.nsObject),
                    //     ),
                    // )
                } else {
                    TODO("Get from registry")
                    // val name = SwiftFqName.External(moduleName, descriptor.name.asString())
                    // SwiftClassSirType(
                    //     SwiftIrTypeDeclaration(
                    //         name = name,
                    //         superTypes = listOf(BuiltinSwiftDeclarations.nsObject),
                    //     ),
                    // )
                }
            }
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny(), swiftExportScope)
        }

        return idType(swiftExportScope)
    }

    private fun idType(swiftExportScope: SwiftExportScope): SwiftNonNullReferenceSirType {
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableSirType
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftAnyObjectSirType
            else -> SwiftAnySirType
        }
    }

    context(SwiftModelScope)
    fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        swiftExportScope: SwiftExportScope,
        returnsVoid: Boolean,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftLambdaSirType {
        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
            functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return SwiftLambdaSirType(
            if (returnsVoid) {
                SwiftVoidSirType
            } else {
                mapReferenceType(
                    functionType.getReturnTypeFromFunctionType(),
                    swiftExportScope.removingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forGenerics(),
                )
            },
            parameterTypes.map {
                mapReferenceType(
                    it,
                    swiftExportScope.addingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forGenerics(),
                )
            },
            isEscaping = swiftExportScope.hasFlag(SwiftExportScope.Flags.Escaping) && !functionType.binaryRepresentationIsNullable(),
        )
    }

    context(SwiftModelScope)
    private fun mapFunctionType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge.BlockPointer,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftReferenceSirType {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType // Should not happen though.
        }

        return mapFunctionTypeIgnoringNullability(functionType, swiftExportScope, typeBridge.returnsVoid, flowMappingStrategy)
            .withNullabilityOf(kotlinType)
    }

    context(SwiftModelScope)
    internal fun mapType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType = when (typeBridge) {
        NativeTypeBridge.Reference -> mapReferenceType(kotlinType, swiftExportScope, flowMappingStrategy)
        is NativeTypeBridge.BlockPointer -> mapFunctionType(kotlinType, swiftExportScope, typeBridge, flowMappingStrategy)
        is NativeTypeBridge.ValueType -> when (typeBridge.objCValueType) {
            ObjCValueType.BOOL -> SwiftPrimitiveSirType.Bool
            ObjCValueType.UNICHAR -> SwiftPrimitiveSirType.unichar
            ObjCValueType.CHAR -> SwiftPrimitiveSirType.Int8
            ObjCValueType.SHORT -> SwiftPrimitiveSirType.Int16
            ObjCValueType.INT -> SwiftPrimitiveSirType.Int32
            ObjCValueType.LONG_LONG -> SwiftPrimitiveSirType.Int64
            ObjCValueType.UNSIGNED_CHAR -> SwiftPrimitiveSirType.UInt8
            ObjCValueType.UNSIGNED_SHORT -> SwiftPrimitiveSirType.UInt16
            ObjCValueType.UNSIGNED_INT -> SwiftPrimitiveSirType.UInt32
            ObjCValueType.UNSIGNED_LONG_LONG -> SwiftPrimitiveSirType.UInt64
            ObjCValueType.FLOAT -> SwiftPrimitiveSirType.Float
            ObjCValueType.DOUBLE -> SwiftPrimitiveSirType.Double
            ObjCValueType.POINTER -> SwiftPointerSirType(SwiftVoidSirType, kotlinType.binaryRepresentationIsNullable())
        }
    }

    context(SwiftModelScope)
    private fun translateClassOrInterfaceName(
        descriptor: ClassDescriptor,
        exportScope: SwiftExportScope,
        typeArgs: List<SwiftNonNullReferenceSirType>,
        ifKotlinType: (KotlinTypeSwiftModel) -> SwiftNonNullReferenceSirType,
    ): SwiftNonNullReferenceSirType {
        assert(descriptor in descriptorProvider.exposedClasses) { "Shouldn't be exposed: $descriptor" }

        if (ErrorUtils.isError(descriptor)) {
            return SwiftErrorSirType
        }

        return if (descriptor.hasSwiftModel) {
            val swiftModel = descriptor.swiftModel
            val bridge = swiftModel.bridge

            when {
                exportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> ifKotlinType(swiftModel)
                exportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> if (bridge != null && bridge.declaration.isHashable()) {
                    SwiftClassSirType(bridge.declaration, typeArgs)
                } else {
                    ifKotlinType(swiftModel)
                }
                else -> swiftModel.bridge?.let { SwiftClassSirType(it.declaration, typeArgs) } ?: swiftModel.let(ifKotlinType)
            }
        } else {
            if (descriptor.kind.isInterface) {
                SwiftProtocolSirType(
                    swiftIrDeclarationRegistry.declarationForInterface(descriptor),
                )
            } else {
                SwiftClassSirType(
                    swiftIrDeclarationRegistry.declarationForClass(descriptor),
                    typeArgs,
                )
            }
            // TODO: ifSwiftType(SwiftFqName.Local.SwiftType(namer.getClassOrProtocolName(descriptor).swiftName))
        }
    }
}

fun SwiftNonNullReferenceSirType.withNullabilityOf(kotlinType: KotlinType): SwiftReferenceSirType =
    if (kotlinType.binaryRepresentationIsNullable()) SwiftNullableReferenceSirType(this) else this
