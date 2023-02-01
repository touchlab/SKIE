@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.SwiftTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridge
import co.touchlab.skie.plugin.api.model.type.bridge.NativeTypeBridge
import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyObjectTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftErrorTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftInstanceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeProtocolTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftLambdaTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftMetaClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNullableReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPointerTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPrimitiveTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftProtocolTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftVoidTypeModel
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
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes

fun SwiftTypeModel.makeNullableIfReferenceOrPointer(): SwiftTypeModel = when (this) {
    is SwiftPointerTypeModel -> SwiftPointerTypeModel(pointee, nullable = true)
    is SwiftNonNullReferenceTypeModel -> SwiftNullableReferenceTypeModel(this)
    is SwiftNullableReferenceTypeModel, is SwiftPrimitiveTypeModel, SwiftVoidTypeModel, SwiftErrorTypeModel -> this
}

internal tailrec fun KotlinType.getErasedTypeClass(): ClassDescriptor =
    TypeUtils.getClassDescriptor(this) ?: this.constructor.supertypes.first().getErasedTypeClass()

class SwiftTypeTranslator(
    private val descriptorProvider: DescriptorProvider,
    val namer: ObjCExportNamer,
    private val problemCollector: SwiftTranslationProblemCollector,
) {

    context(SwiftModelScope)
    internal fun mapReturnType(
        returnBridge: MethodBridge.ReturnValue,
        method: FunctionDescriptor,
        swiftExportScope: SwiftExportScope,
    ): SwiftTypeModel {
        return when (returnBridge) {
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void,
            -> SwiftVoidTypeModel
            MethodBridge.ReturnValue.HashCode -> SwiftPrimitiveTypeModel.NSUInteger
            is MethodBridge.ReturnValue.Mapped -> mapType(method.returnType!!, swiftExportScope, returnBridge.bridge)
            MethodBridge.ReturnValue.WithError.Success -> SwiftVoidTypeModel
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(returnBridge.successBridge, method, swiftExportScope)

                if (!returnBridge.successMayBeZero) {
                    check(
                        successReturnType is SwiftNonNullReferenceTypeModel
                            || (successReturnType is SwiftPointerTypeModel && !successReturnType.nullable)
                    ) {
                        "Unexpected return type: $successReturnType in $method"
                    }
                }

                successReturnType.makeNullableIfReferenceOrPointer()
            }
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> SwiftInstanceTypeModel
        }
    }

    context(SwiftModelScope)
    internal fun mapReferenceType(kotlinType: KotlinType, swiftExportScope: SwiftExportScope): SwiftReferenceTypeModel =
        mapReferenceTypeIgnoringNullability(kotlinType, swiftExportScope).withNullabilityOf(kotlinType)

    private fun SwiftNonNullReferenceTypeModel.withNullabilityOf(kotlinType: KotlinType): SwiftReferenceTypeModel =
        if (kotlinType.binaryRepresentationIsNullable()) SwiftNullableReferenceTypeModel(this) else this

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullability(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceTypeModel {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: CustomTypeMapper)

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
            return it.mapper.mapType(it.type, this, swiftExportScope)
        }

        if (kotlinType.isTypeParameter()) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> return SwiftAnyHashableTypeModel
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

        if (!descriptorProvider.isTransitivelyExposed(classDescriptor)) {
            return idType(swiftExportScope)
        }

        return if (classDescriptor.kind.isInterface) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
                else -> {
                    translateClassOrInterfaceName(
                        descriptor = classDescriptor,
                        exportScope = swiftExportScope,
                        ifKotlinType = { SwiftKotlinTypeProtocolTypeModel(it) },
                        ifSwiftBridge = {
                            // TODO: This mapping won't probably work very well
                            SwiftProtocolTypeModel(it.stableFqName)
                        },
                        ifSwiftType = { SwiftProtocolTypeModel(it) },
                    )
                }
            }
        } else {
            val typeParamScope = swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType)
            val typeArgs = kotlinType.arguments.map { typeProjection ->
                if (typeProjection.isStarProjection) {
                    idType(typeParamScope)
                } else {
                    mapReferenceTypeIgnoringNullability(typeProjection.type, typeParamScope)
                }
            }

            translateClassOrInterfaceName(
                descriptor = classDescriptor,
                exportScope = swiftExportScope,
                ifKotlinType = { SwiftKotlinTypeClassTypeModel(it, typeArgs) },
                ifSwiftBridge = { SwiftClassTypeModel(it.stableFqName, typeArgs) },
                ifSwiftType = { SwiftClassTypeModel(it, typeArgs) },
            )
        }
    }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(
        descriptor: ClassDescriptor,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceTypeModel {
        if (descriptor.isObjCMetaClass()) return SwiftMetaClassTypeModel
        if (descriptor.isObjCProtocolClass()) return foreignClassType("Protocol")

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            return if (descriptor.kind.isInterface) {
                val name = descriptor.name.asString().removeSuffix("Protocol")
                foreignProtocolType(name)
            } else {
                val name = descriptor.name.asString()
                foreignClassType(name)
            }
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny(), swiftExportScope)
        }

        return idType(swiftExportScope)
    }

    private fun foreignProtocolType(name: String): SwiftProtocolTypeModel {
        // generator?.referenceProtocol(name)
        return SwiftProtocolTypeModel(name)
    }

    private fun foreignClassType(name: String): SwiftClassTypeModel {
        // generator?.referenceClass(ObjCClassForwardDeclaration(name))
        return SwiftClassTypeModel(name)
    }

    private fun idType(swiftExportScope: SwiftExportScope): SwiftNonNullReferenceTypeModel {
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftAnyObjectTypeModel
            else -> SwiftAnyTypeModel
        }
    }

    context(SwiftModelScope)
    fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        swiftExportScope: SwiftExportScope,
        returnsVoid: Boolean,
    ): SwiftLambdaTypeModel {
        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
            functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return SwiftLambdaTypeModel(
            if (returnsVoid) {
                SwiftVoidTypeModel
            } else {
                mapReferenceType(
                    functionType.getReturnTypeFromFunctionType(),
                    swiftExportScope.removingFlags(SwiftExportScope.Flags.Escaping)
                )
            },
            parameterTypes.map { mapReferenceType(it, swiftExportScope.addingFlags(SwiftExportScope.Flags.Escaping)) },
            isEscaping = swiftExportScope.hasFlag(SwiftExportScope.Flags.Escaping) && !functionType.binaryRepresentationIsNullable(),
        )
    }

    context(SwiftModelScope)
    private fun mapFunctionType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge.BlockPointer,
    ): SwiftReferenceTypeModel {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType // Should not happen though.
        }

        return mapFunctionTypeIgnoringNullability(functionType, swiftExportScope, typeBridge.returnsVoid)
            .withNullabilityOf(kotlinType)
    }

    context(SwiftModelScope)
    internal fun mapType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge,
    ): SwiftTypeModel = when (typeBridge) {
        NativeTypeBridge.Reference -> mapReferenceType(kotlinType, swiftExportScope)
        is NativeTypeBridge.BlockPointer -> mapFunctionType(kotlinType, swiftExportScope, typeBridge)
        is NativeTypeBridge.ValueType -> when (typeBridge.objCValueType) {
            ObjCValueType.BOOL -> SwiftPrimitiveTypeModel.Bool
            ObjCValueType.UNICHAR -> SwiftPrimitiveTypeModel.unichar
            ObjCValueType.CHAR -> SwiftPrimitiveTypeModel.Int8
            ObjCValueType.SHORT -> SwiftPrimitiveTypeModel.Int16
            ObjCValueType.INT -> SwiftPrimitiveTypeModel.Int32
            ObjCValueType.LONG_LONG -> SwiftPrimitiveTypeModel.Int64
            ObjCValueType.UNSIGNED_CHAR -> SwiftPrimitiveTypeModel.UInt8
            ObjCValueType.UNSIGNED_SHORT -> SwiftPrimitiveTypeModel.UInt16
            ObjCValueType.UNSIGNED_INT -> SwiftPrimitiveTypeModel.UInt32
            ObjCValueType.UNSIGNED_LONG_LONG -> SwiftPrimitiveTypeModel.UInt64
            ObjCValueType.FLOAT -> SwiftPrimitiveTypeModel.Float
            ObjCValueType.DOUBLE -> SwiftPrimitiveTypeModel.Double
            ObjCValueType.POINTER -> SwiftPointerTypeModel(SwiftVoidTypeModel, kotlinType.binaryRepresentationIsNullable())
        }
    }

    context(SwiftModelScope)
    private fun translateClassOrInterfaceName(
        descriptor: ClassDescriptor,
        exportScope: SwiftExportScope,
        ifKotlinType: (KotlinTypeSwiftModel) -> SwiftNonNullReferenceTypeModel,
        ifSwiftBridge: (TypeSwiftModel) -> SwiftNonNullReferenceTypeModel,
        ifSwiftType: (String) -> SwiftNonNullReferenceTypeModel,
    ): SwiftNonNullReferenceTypeModel {
        assert(descriptorProvider.isTransitivelyExposed(descriptor)) { "Shouldn't be exposed: $descriptor" }

        if (ErrorUtils.isError(descriptor)) {
            return SwiftErrorTypeModel
        }

        return if (descriptor.hasSwiftModel) {
            val swiftModel = descriptor.swiftModel
            val bridge = swiftModel.bridge

            when {
                exportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> ifKotlinType(swiftModel)
                exportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> if (bridge is SwiftTypeSwiftModel && bridge.isHashable) {
                    ifSwiftBridge(bridge)
                } else {
                    ifKotlinType(swiftModel)
                }
                else -> swiftModel.bridge?.let(ifSwiftBridge) ?: swiftModel.let(ifKotlinType)
            }
        } else {
            ifSwiftType(namer.getClassOrProtocolName(descriptor).swiftName)
        }
    }
}
