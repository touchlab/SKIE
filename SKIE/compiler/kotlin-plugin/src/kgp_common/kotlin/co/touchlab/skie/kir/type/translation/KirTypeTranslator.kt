@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.ErrorOutKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SuspendCompletionKirType
import co.touchlab.skie.oir.type.OirType
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
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import org.jetbrains.kotlin.types.typeUtil.supertypes

class KirTypeTranslator {

    internal fun mapValueParameterType(
        functionDescriptor: FunctionDescriptor,
        valueParameterDescriptor: ParameterDescriptor?,
        bridge: MethodBridgeValueParameter,
    ): KirType =
        when (bridge) {
            is MethodBridgeValueParameter.Mapped -> mapType(valueParameterDescriptor!!.type, bridge.bridge)
            MethodBridgeValueParameter.ErrorOutParameter -> ErrorOutKirType
            is MethodBridgeValueParameter.SuspendCompletion -> {
                SuspendCompletionKirType(functionDescriptor.returnType!!, useUnitCompletion = bridge.useUnitCompletion)
            }
        }

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

    private fun mapType(kotlinType: KotlinType, typeBridge: TypeBridge): KirType =
        when (typeBridge) {
            ReferenceBridge -> ReferenceKirType(kotlinType)
            is BlockPointerBridge -> mapFunctionType(kotlinType, typeBridge)
            is ValueTypeBridge -> mapValueType(kotlinType, typeBridge)
        }

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

        return BlockPointerKirType(functionType, typeBridge.returnsVoid)
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

    private fun OirType.toKirType(): KirType =
        OirBasedKirType(this)

    private fun KirType.makeNullableIfReferenceOrPointer(): KirType =
        when (this) {
            is BlockPointerKirType -> this.copy(kotlinType = kotlinType.makeNullable())
            is OirBasedKirType -> this.makeNullableIfReferenceOrPointer()
            is ReferenceKirType -> this.copy(kotlinType = kotlinType.makeNullable())
            is SuspendCompletionKirType -> this.copy(kotlinType = kotlinType.makeNullable())
            ErrorOutKirType -> this
        }

    private fun OirBasedKirType.makeNullableIfReferenceOrPointer(): KirType =
        when (this.oirType) {
            is PointerOirType -> this.oirType.copy(nullable = true).toKirType()
            is PrimitiveOirType -> this
            VoidOirType -> this
            else -> error("Unsupported OirBasedKirType type: $this")
        }
}
