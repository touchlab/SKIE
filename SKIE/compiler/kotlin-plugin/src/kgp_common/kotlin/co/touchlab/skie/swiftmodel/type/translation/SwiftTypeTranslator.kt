@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.swiftmodel.type.translation

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridge
import co.touchlab.skie.swiftmodel.type.bridge.NativeTypeBridge
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.KotlinErrorSirType
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.NonNullSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SkieErrorSirType
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
import org.jetbrains.kotlin.types.checker.intersectWrappedTypes
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.hasTypeParameterRecursiveBounds
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections
import org.jetbrains.kotlin.types.typeUtil.supertypes

class SwiftTypeTranslator(
    private val descriptorProvider: DescriptorProvider,
    val namer: ObjCExportNamer,
    val problemCollector: SwiftTranslationProblemCollector,
    val builtinSwiftBridgeableProvider: BuiltinSwiftBridgeableProvider,
    val sirProvider: SirProvider,
) {

    private val sirBuiltins = sirProvider.sirBuiltins

    private val customTypeMappers = CustomTypeMappers(sirBuiltins)

    context(SwiftModelScope)
    internal fun mapFileType(sourceFile: SourceFile): SirType {
        val fileModel = sourceFile.swiftModel

        return DeclaredSirType(fileModel.primarySirClass)
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
            -> sirBuiltins.Swift.Void.defaultType
            MethodBridge.ReturnValue.HashCode -> sirBuiltins.Swift.UInt.defaultType
            is MethodBridge.ReturnValue.Mapped -> mapType(
                method.returnType!!,
                swiftExportScope,
                returnBridge.bridge,
                flowMappingStrategy,
            )
            MethodBridge.ReturnValue.WithError.Success -> sirBuiltins.Swift.Void.defaultType
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(returnBridge.successBridge, method, swiftExportScope, flowMappingStrategy)

                if (!returnBridge.successMayBeZero) {
                    check(
                        successReturnType is NonNullSirType,
                    ) {
                        "Unexpected return type: $successReturnType in $method"
                    }
                }

                successReturnType.makeNullableIfNotPrimitive()
            }
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> SpecialSirType.Self
        }
    }

    context(SwiftModelScope)
    internal fun mapReferenceType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType =
        mapReferenceTypeIgnoringNullability(kotlinType, swiftExportScope, flowMappingStrategy).withNullabilityOf(kotlinType)

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullability(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): NonNullSirType {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: CustomTypeMapper)

        if (flowMappingStrategy == FlowMappingStrategy.Full) {
            val flowMapper = FlowTypeMappers.getMapperOrNull(kotlinType)
            if (flowMapper != null) {
                return flowMapper.mapType(kotlinType, this, swiftExportScope, flowMappingStrategy)
            }
        }

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                customTypeMappers.getMapper(descriptor)?.let { mapper ->
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
                        "This most likely wouldn't work as expected.",
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
    ): NonNullSirType {
        if (kotlinType.isTypeParameter()) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> return sirBuiltins.Swift.AnyHashable.defaultType
                else -> {
                    TypeUtils.getTypeParameterDescriptorOrNull(kotlinType)?.let { typeParameterDescriptor ->
                        val genericTypeUsage = swiftExportScope.genericScope.getGenericTypeUsage(typeParameterDescriptor)
                        if (genericTypeUsage != null) {
                            return genericTypeUsage
                        } else if (hasTypeParameterRecursiveBounds(typeParameterDescriptor)) {
                            val erasedType = intersectWrappedTypes(
                                kotlinType.immediateSupertypes().map {
                                    /* The commented out code below keeps more type information, but because that information is dropped by
                                        a probably missing functionallity in the Kotlin compiler, we have to erase it all to a star projection anyway. */
                                    it.replaceArgumentsWithStarProjections()
                                    // it.replace(newArguments = it.constructor.parameters.zip(it.arguments) { parameter, argument ->
                                    //     if (argument.type.constructor == kotlinType.constructor) {
                                    //         StarProjectionImpl(parameter)
                                    //     } else {
                                    //         argument
                                    //     }
                                    // })
                                },
                            )
                            return mapReferenceTypeIgnoringNullability(
                                erasedType,
                                swiftExportScope,
                                flowMappingStrategy.forTypeArgumentsOf(kotlinType),
                            )
                        } else if (kotlinType.immediateSupertypes().singleOrNull()?.isTypeParameter() == true) {
                            val referencedType = kotlinType.immediateSupertypes().single()
                            return mapReferenceTypeIgnoringNullability(
                                referencedType,
                                swiftExportScope,
                                flowMappingStrategy.forTypeArgumentsOf(referencedType),
                            )
                        }
                    }
                }
            }
        }

        val (kotlinType, classDescriptor) = kotlinType.getErasedTypeClass()

        if (KotlinBuiltIns.isAny(classDescriptor) || classDescriptor.classId in customTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
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
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> sirBuiltins.Swift.AnyHashable.defaultType
                else -> {
                    translateClassOrInterfaceName(
                        descriptor = classDescriptor,
                        exportScope = swiftExportScope,
                        typeArgs = { emptyList() },
                        ifKotlinType = { model, _ ->
                            DeclaredSirType(model.kotlinSirClass)
                        },
                    )
                }
            }
        } else {
            translateClassOrInterfaceName(
                descriptor = classDescriptor,
                exportScope = swiftExportScope,
                typeArgs = { typeParamScope ->
                    kotlinType.arguments.map { typeProjection ->
                        if (typeProjection.isStarProjection) {
                            idType(typeParamScope)
                        } else {
                            mapReferenceTypeIgnoringNullability(
                                typeProjection.type,
                                typeParamScope,
                                flowMappingStrategy.forTypeArgumentsOf(kotlinType),
                            )
                        }
                    }
                },
                ifKotlinType = { model, typeArgs ->
                    DeclaredSirType(model.kotlinSirClass, typeArgs)
                },
            )
        }
    }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(
        descriptor: ClassDescriptor,
        swiftExportScope: SwiftExportScope,
    ): NonNullSirType {
        if (descriptor.isObjCMetaClass()) return sirBuiltins.Swift.AnyClass.defaultType
        if (descriptor.isObjCProtocolClass()) return SpecialSirType.Protocol

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            val bridge = builtinSwiftBridgeableProvider.bridgeFor(descriptor.fqNameSafe, swiftExportScope)

            return bridge ?: sirProvider.getExternalTypeDeclaration(descriptor).defaultType
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny(), swiftExportScope)
        }

        return idType(swiftExportScope)
    }

    private fun idType(swiftExportScope: SwiftExportScope): NonNullSirType {
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> sirBuiltins.Swift.AnyHashable.defaultType
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> sirBuiltins.Swift.AnyObject.defaultType
            else -> SpecialSirType.Any
        }
    }

    context(SwiftModelScope)
    fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        swiftExportScope: SwiftExportScope,
        returnsVoid: Boolean,
        flowMappingStrategy: FlowMappingStrategy,
    ): NonNullSirType {
        if (swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType)) {
            return SkieErrorSirType.Lambda
        }

        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
                functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return LambdaSirType(
            if (returnsVoid) {
                sirBuiltins.Swift.Void.defaultType
            } else {
                mapReferenceType(
                    functionType.getReturnTypeFromFunctionType(),
                    swiftExportScope.removingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forTypeArgumentsOf(functionType),
                )
            },
            parameterTypes.map {
                mapReferenceType(
                    it,
                    swiftExportScope.addingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forTypeArgumentsOf(functionType),
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
    ): SirType {
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
            ObjCValueType.BOOL -> sirBuiltins.Swift.Bool.defaultType
            ObjCValueType.UNICHAR -> sirBuiltins.Foundation.unichar.defaultType
            ObjCValueType.CHAR -> sirBuiltins.Swift.Int8.defaultType
            ObjCValueType.SHORT -> sirBuiltins.Swift.Int16.defaultType
            ObjCValueType.INT -> sirBuiltins.Swift.Int32.defaultType
            ObjCValueType.LONG_LONG -> sirBuiltins.Swift.Int64.defaultType
            ObjCValueType.UNSIGNED_CHAR -> sirBuiltins.Swift.UInt8.defaultType
            ObjCValueType.UNSIGNED_SHORT -> sirBuiltins.Swift.UInt16.defaultType
            ObjCValueType.UNSIGNED_INT -> sirBuiltins.Swift.UInt32.defaultType
            ObjCValueType.UNSIGNED_LONG_LONG -> sirBuiltins.Swift.UInt64.defaultType
            ObjCValueType.FLOAT -> sirBuiltins.Swift.Float.defaultType
            ObjCValueType.DOUBLE -> sirBuiltins.Swift.Double.defaultType
            ObjCValueType.POINTER -> sirBuiltins.Swift.UnsafeMutableRawPointer.defaultType.withNullabilityOf(kotlinType)
        }
    }

    context(SwiftModelScope)
    private fun translateClassOrInterfaceName(
        descriptor: ClassDescriptor,
        exportScope: SwiftExportScope,
        typeArgs: (SwiftExportScope) -> List<NonNullSirType>,
        ifKotlinType: (model: KotlinTypeSwiftModel, typeArgs: List<NonNullSirType>) -> NonNullSirType,
    ): NonNullSirType {
        assert(descriptor in descriptorProvider.exposedClasses) { "Should be exposed: $descriptor" }

        if (ErrorUtils.isError(descriptor)) {
            return KotlinErrorSirType
        }

        fun swiftTypeArgs(): List<NonNullSirType> = typeArgs(exportScope)
        fun referenceTypeArgs(): List<NonNullSirType> =
            typeArgs(exportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType))

        return if (descriptor.hasSwiftModel) {
            val swiftModel = descriptor.swiftModel
            val bridge = swiftModel.bridgedSirClass

            when {
                exportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> ifKotlinType(swiftModel, referenceTypeArgs())
                exportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> if (bridge != null && bridge.isHashable) {
                    DeclaredSirType(bridge, swiftTypeArgs())
                } else {
                    ifKotlinType(swiftModel, referenceTypeArgs())
                }
                else -> bridge?.let {
                    DeclaredSirType(it, swiftTypeArgs())
                } ?: ifKotlinType(swiftModel, referenceTypeArgs())
            }
        } else {
            mapObjCObjectReferenceTypeIgnoringNullability(descriptor, exportScope)
        }
    }
}


fun SirType.makeNullableIfNotPrimitive(): SirType =
    if (this is NonNullSirType && !isPrimitive) NullableSirType(this) else this

fun NonNullSirType.withNullabilityOf(kotlinType: KotlinType): SirType =
    if (kotlinType.binaryRepresentationIsNullable()) NullableSirType(this) else this

internal tailrec fun KotlinType.getErasedTypeClass(): Pair<KotlinType, ClassDescriptor> =
    TypeUtils.getClassDescriptor(this)?.let { this to it } ?: this.constructor.supertypes.first().getErasedTypeClass()
