@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.factory

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter
import co.touchlab.skie.plugin.api.model.type.bridge.NativeTypeBridge
import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportTranslatorImplReflector
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCBlockPointerType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCClassType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportProblemCollector
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportScope
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportTranslatorImpl
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCInstanceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCNonNullReferenceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCNoneExportScope
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCNullableReferenceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCPointerType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCPrimitiveType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCVoidType
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.makeNullableIfReferenceOrPointer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.replaceArguments
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.model.TypeArgumentMarker
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType as KotlinObjCValueType

class ObjCTypeProvider(
    private val descriptorProvider: DescriptorProvider,
    private val swiftModelScope: SwiftModelScope,
    namer: ObjCExportNamer,
) {

    private val mapper = namer.mapper

    private val translator = ObjCExportTranslatorImpl(null, mapper, namer, ObjCExportProblemCollector.SILENT, true)

    private val reflectedTranslator = translator.reflectedBy<ObjCExportTranslatorImplReflector>()

    fun getFunctionParameterType(
        function: FunctionDescriptor,
        parameter: ParameterDescriptor?,
        bridge: MethodBridgeParameter.ValueParameter,
        isFlowMappingEnabled: Boolean,
        genericExportScope: ObjCExportScope = createGenericExportScope(function),
    ): ObjCType = when (bridge) {
        is MethodBridgeParameter.ValueParameter.Mapped -> {
            mapType(parameter!!.type, bridge.bridge.toKotlinVersion(), genericExportScope, isFlowMappingEnabled)
        }
        MethodBridgeParameter.ValueParameter.ErrorOutParameter ->
            ObjCPointerType(ObjCNullableReferenceType(ObjCClassType("NSError")), nullable = true)
        is MethodBridgeParameter.ValueParameter.SuspendCompletion -> {
            val resultType = if (bridge.useUnitCompletion) {
                null
            } else {
                when (val it =
                    translator.mapReferenceType(function.returnType!!.substituteFlows(isFlowMappingEnabled), genericExportScope)) {
                    is ObjCNonNullReferenceType -> ObjCNullableReferenceType(it, isNullableResult = false)
                    is ObjCNullableReferenceType -> ObjCNullableReferenceType(it.nonNullType, isNullableResult = true)
                }
            }
            ObjCBlockPointerType(
                returnType = ObjCVoidType,
                parameterTypes = listOfNotNull(
                    resultType,
                    ObjCNullableReferenceType(ObjCClassType("NSError"))
                )
            )
        }
    }

    fun getFunctionReturnType(baseFunction: FunctionDescriptor, function: FunctionDescriptor): ObjCType {
        val bridge = mapper.bridgeMethod(baseFunction)

        val genericExportScope = createGenericExportScope(function)

        return mapReturnType(bridge.returnBridge, function, genericExportScope)
    }

    fun getPropertyType(baseProperty: PropertyDescriptor, property: PropertyDescriptor): ObjCType {
        val getterBridge = mapper.bridgeMethod(baseProperty.getter!!)

        val genericExportScope = createGenericExportScope(property)

        return mapReturnType(getterBridge.returnBridge, property.getter!!, genericExportScope)
    }

    private fun createGenericExportScope(descriptor: CallableMemberDescriptor): ObjCExportScope =
        descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)?.let {
            translator.createGenericExportScope(it)
        } ?: ObjCNoneExportScope

    private fun mapReturnType(
        returnBridge: MethodBridge.ReturnValue,
        method: FunctionDescriptor,
        objCExportScope: ObjCExportScope,
    ): ObjCType = when (returnBridge) {
        MethodBridge.ReturnValue.Suspend,
        MethodBridge.ReturnValue.Void,
        -> ObjCVoidType
        MethodBridge.ReturnValue.HashCode -> ObjCPrimitiveType.NSUInteger
        is MethodBridge.ReturnValue.Mapped -> mapType(method.returnType!!, returnBridge.bridge, objCExportScope, true)
        MethodBridge.ReturnValue.WithError.Success -> ObjCPrimitiveType.BOOL
        is MethodBridge.ReturnValue.WithError.ZeroForError -> {
            val successReturnType = mapReturnType(returnBridge.successBridge, method, objCExportScope)

            if (!returnBridge.successMayBeZero) {
                check(
                    successReturnType is ObjCNonNullReferenceType
                        || (successReturnType is ObjCPointerType && !successReturnType.nullable)
                ) {
                    "Unexpected return type: $successReturnType in $method"
                }
            }

            successReturnType.makeNullableIfReferenceOrPointer()
        }

        MethodBridge.ReturnValue.Instance.InitResult,
        MethodBridge.ReturnValue.Instance.FactoryResult,
        -> ObjCInstanceType
    }

    private fun mapType(
        kotlinType: KotlinType,
        typeBridge: TypeBridge,
        objCExportScope: ObjCExportScope,
        isFlowMappingEnabled: Boolean,
    ): ObjCType {
        val substitutedType = kotlinType.substituteFlows(isFlowMappingEnabled)

        return reflectedTranslator.mapType.invoke(substitutedType, typeBridge, objCExportScope)
    }

    private fun KotlinType.substituteFlows(isFlowMappingEnabled: Boolean): KotlinType {
        val supportedFlow = SupportedFlow.from(this)

        if (!isFlowMappingEnabled || supportedFlow == null) {
            return this.withSubstitutedArgumentsForFlow()
        }

        return supportedFlow.createType(this)
    }

    private fun SupportedFlow.createType(originalType: KotlinType): KotlinType {
        val substitutedArguments = originalType.arguments.map { it.substituteFlows() }

        val hasNullableTypeArgument = originalType.arguments.any { it.type.isNullable() }
        val substituteFqName = if (hasNullableTypeArgument) this.toOptionalFqName else this.toNonOptionalFqName
        val substitute = swiftModelScope.referenceClass(substituteFqName).classDescriptor.defaultType

        return KotlinTypeFactory.simpleType(substitute, arguments = substitutedArguments).makeNullableAsSpecified(originalType.isNullable())
    }

    private fun KotlinType.withSubstitutedArgumentsForFlow(): KotlinType =
        replaceArguments { it.substituteFlows() } as KotlinType

    private fun TypeArgumentMarker.substituteFlows(): TypeProjection =
        when (this) {
            is TypeProjectionImpl -> {
                val substitutedType = type.substituteFlows(true)

                if (this.type != substitutedType) TypeProjectionImpl(projectionKind, substitutedType) else this
            }
            is TypeProjection -> this
            else -> error("Unsupported type argument $this.")
        }
}

private fun NativeTypeBridge.toKotlinVersion(): TypeBridge =
    when (this) {
        NativeTypeBridge.Reference -> ReferenceBridge
        is NativeTypeBridge.BlockPointer -> BlockPointerBridge(numberOfParameters, returnsVoid)
        is NativeTypeBridge.ValueType -> ValueTypeBridge(objCValueType.toKotlinVersion())
    }

private fun ObjCValueType.toKotlinVersion(): KotlinObjCValueType =
    when (this) {
        ObjCValueType.BOOL -> KotlinObjCValueType.BOOL
        ObjCValueType.UNICHAR -> KotlinObjCValueType.UNICHAR
        ObjCValueType.CHAR -> KotlinObjCValueType.CHAR
        ObjCValueType.SHORT -> KotlinObjCValueType.SHORT
        ObjCValueType.INT -> KotlinObjCValueType.INT
        ObjCValueType.LONG_LONG -> KotlinObjCValueType.LONG_LONG
        ObjCValueType.UNSIGNED_CHAR -> KotlinObjCValueType.UNSIGNED_CHAR
        ObjCValueType.UNSIGNED_SHORT -> KotlinObjCValueType.UNSIGNED_SHORT
        ObjCValueType.UNSIGNED_INT -> KotlinObjCValueType.UNSIGNED_INT
        ObjCValueType.UNSIGNED_LONG_LONG -> KotlinObjCValueType.UNSIGNED_LONG_LONG
        ObjCValueType.FLOAT -> KotlinObjCValueType.FLOAT
        ObjCValueType.DOUBLE -> KotlinObjCValueType.DOUBLE
        ObjCValueType.POINTER -> KotlinObjCValueType.POINTER
    }
