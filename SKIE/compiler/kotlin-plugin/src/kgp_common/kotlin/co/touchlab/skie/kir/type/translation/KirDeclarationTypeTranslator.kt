@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.NullableReferenceKirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.PointerKirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import co.touchlab.skie.oir.type.VoidOirType
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.supertypes

class KirDeclarationTypeTranslator(
    private val kirTypeTranslator: KirTypeTranslator,
    kirBuiltins: KirBuiltins,
) : KirTypeTranslatorUtilityScope() {

    private val nullableNSErrorType = kirBuiltins.NSError.defaultType.withNullabilityOf(true)

    context(KirTypeParameterScope)
    internal fun mapValueParameterType(
        functionDescriptor: FunctionDescriptor,
        valueParameterDescriptor: ParameterDescriptor?,
        bridge: MethodBridgeValueParameter,
    ): KirType =
        when (bridge) {
            is MethodBridgeValueParameter.Mapped -> mapType(valueParameterDescriptor!!.type, bridge.bridge)
            MethodBridgeValueParameter.ErrorOutParameter -> {
                PointerKirType(nullableNSErrorType, nullable = true)
            }
            is MethodBridgeValueParameter.SuspendCompletion -> {
                mapSuspendCompletionType(functionDescriptor.returnType!!, useUnitCompletion = bridge.useUnitCompletion)
            }
        }

    context(KirTypeParameterScope)
    internal fun mapReturnType(
        descriptor: FunctionDescriptor,
        returnBridge: MethodBridge.ReturnValue,
    ): KirType =
        when (returnBridge) {
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void,
            -> VoidOirType.toKirType()
            MethodBridge.ReturnValue.HashCode -> PrimitiveOirType.NSUInteger.toKirType()
            is MethodBridge.ReturnValue.Mapped -> mapType(descriptor.returnType!!, returnBridge.bridge)
            MethodBridge.ReturnValue.WithError.Success -> PrimitiveOirType.BOOL.toKirType()
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(descriptor, returnBridge.successBridge)

                successReturnType.makeNullableIfReferenceOrPointer()
            }

            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> SpecialReferenceOirType.InstanceType.toKirType()
        }

    context(KirTypeParameterScope)
    private fun mapType(kotlinType: KotlinType, typeBridge: TypeBridge): KirType =
        when (typeBridge) {
            ReferenceBridge -> kirTypeTranslator.mapReferenceType(kotlinType)
            is BlockPointerBridge -> mapFunctionType(kotlinType, typeBridge)
            is ValueTypeBridge -> mapValueType(kotlinType, typeBridge)
        }

    context(KirTypeParameterScope)
    private fun mapSuspendCompletionType(kotlinType: KotlinType, useUnitCompletion: Boolean): BlockPointerKirType {
        val resultType = if (useUnitCompletion) {
            null
        } else {
            when (val it = kirTypeTranslator.mapReferenceType(kotlinType)) {
                is NonNullReferenceKirType -> NullableReferenceKirType(it, isNullableResult = false)
                is NullableReferenceKirType -> NullableReferenceKirType(it.nonNullType, isNullableResult = true)
            }
        }

        return BlockPointerKirType(
            valueParameterTypes = listOfNotNull(
                resultType,
                nullableNSErrorType,
            ),
            returnType = VoidOirType.toKirType(),
        )
    }

    context(KirTypeParameterScope)
    private fun mapFunctionType(
        kotlinType: KotlinType,
        typeBridge: BlockPointerBridge,
    ): KirType {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        // Somewhat similar to mapType:
        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType // Should not happen though.
        }

        return kirTypeTranslator.mapFunctionType(functionType, typeBridge.returnsVoid)
    }

    private fun mapValueType(
        kotlinType: KotlinType,
        typeBridge: ValueTypeBridge,
    ): KirType =
        when (typeBridge.objCValueType) {
            ObjCValueType.BOOL -> PrimitiveOirType.BOOL
            ObjCValueType.UNICHAR -> PrimitiveOirType.unichar
            ObjCValueType.CHAR -> PrimitiveOirType.int8_t
            ObjCValueType.SHORT -> PrimitiveOirType.int16_t
            ObjCValueType.INT -> PrimitiveOirType.int32_t
            ObjCValueType.LONG_LONG -> PrimitiveOirType.int64_t
            ObjCValueType.UNSIGNED_CHAR -> PrimitiveOirType.uint8_t
            ObjCValueType.UNSIGNED_SHORT -> PrimitiveOirType.uint16_t
            ObjCValueType.UNSIGNED_INT -> PrimitiveOirType.uint32_t
            ObjCValueType.UNSIGNED_LONG_LONG -> PrimitiveOirType.uint64_t
            ObjCValueType.FLOAT -> PrimitiveOirType.float
            ObjCValueType.DOUBLE -> PrimitiveOirType.double
            ObjCValueType.POINTER -> PointerOirType(VoidOirType, kotlinType.binaryRepresentationIsNullable())
        }.toKirType()

    private fun KirType.makeNullableIfReferenceOrPointer(): KirType =
        when (this) {
            is NullableReferenceKirType -> this
            is NonNullReferenceKirType -> NullableReferenceKirType(this)
            is PointerKirType -> this.copy(nullable = true)
            is OirBasedKirType -> this.makeNullableIfReferenceOrPointer()
        }

    private fun OirBasedKirType.makeNullableIfReferenceOrPointer(): KirType =
        when (this.oirType) {
            is PointerOirType -> this.oirType.copy(nullable = true).toKirType()
            is PrimitiveOirType -> this
            VoidOirType -> this
            else -> error("Unsupported OirBasedKirType type: $this")
        }
}
