@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge as InternalMethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge as InternalTypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge as InternalReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge as InternalBlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge as InternalValueTypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeReceiver as InternalMethodBridgeReceiver
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter as InternalMethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType as InternalObjCValueType
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper

class DescriptorBridgeProvider internal constructor(
    private val mapper: ObjCExportMapper,
) {
    constructor(namer: ObjCExportNamer): this(namer.mapper)

    internal fun bridgeMethod(descriptor: FunctionDescriptor): MethodBridge {
        return mapper.bridgeMethod(descriptor).toMethodBridge()
    }

    private fun InternalMethodBridge.toMethodBridge(): MethodBridge {
        return MethodBridge(
            returnBridge = returnBridge.toReturnValue(),
            receiver = receiver.toMethodBridgeReceiver(),
            valueParameters = valueParameters.map { it.toValueParameter() },
        )
    }

    private fun InternalMethodBridge.ReturnValue.toReturnValue(): MethodBridge.ReturnValue {
        return when (this) {
            InternalMethodBridge.ReturnValue.HashCode -> MethodBridge.ReturnValue.HashCode
            InternalMethodBridge.ReturnValue.Instance.FactoryResult -> MethodBridge.ReturnValue.Instance.FactoryResult
            InternalMethodBridge.ReturnValue.Instance.InitResult -> MethodBridge.ReturnValue.Instance.InitResult
            InternalMethodBridge.ReturnValue.Suspend -> MethodBridge.ReturnValue.Suspend
            InternalMethodBridge.ReturnValue.Void -> MethodBridge.ReturnValue.Void
            InternalMethodBridge.ReturnValue.WithError.Success -> MethodBridge.ReturnValue.WithError.Success
            is InternalMethodBridge.ReturnValue.Mapped -> MethodBridge.ReturnValue.Mapped(
                bridge = bridge.toTypeBridge(),
            )
            is InternalMethodBridge.ReturnValue.WithError.ZeroForError -> MethodBridge.ReturnValue.WithError.ZeroForError(
                successBridge = successBridge.toReturnValue(),
                successMayBeZero = successMayBeZero,
            )
        }
    }

    private fun InternalMethodBridgeReceiver.toMethodBridgeReceiver(): MethodBridgeParameter.Receiver {
        return when (this) {
            InternalMethodBridgeReceiver.Factory -> MethodBridgeParameter.Receiver.Factory
            InternalMethodBridgeReceiver.Instance -> MethodBridgeParameter.Receiver.Instance
            InternalMethodBridgeReceiver.Static -> MethodBridgeParameter.Receiver.Static
        }
    }

    private fun InternalMethodBridgeValueParameter.toValueParameter(): MethodBridgeParameter.ValueParameter {
        return when (this) {
            InternalMethodBridgeValueParameter.ErrorOutParameter -> MethodBridgeParameter.ValueParameter.ErrorOutParameter
            is InternalMethodBridgeValueParameter.Mapped -> MethodBridgeParameter.ValueParameter.Mapped(bridge.toTypeBridge())
            is InternalMethodBridgeValueParameter.SuspendCompletion -> MethodBridgeParameter.ValueParameter.SuspendCompletion(
                useUnitCompletion = useUnitCompletion,
            )
        }
    }

    private fun InternalTypeBridge.toTypeBridge(): NativeTypeBridge {
        return when (this) {
            is InternalReferenceBridge -> NativeTypeBridge.Reference
            is InternalBlockPointerBridge -> NativeTypeBridge.BlockPointer(
                numberOfParameters = numberOfParameters,
                returnsVoid = returnsVoid,
            )
            is InternalValueTypeBridge -> NativeTypeBridge.ValueType(
                objCValueType = objCValueType.toObjCValueType(),
            )
        }
    }

    private fun InternalObjCValueType.toObjCValueType(): ObjCValueType {
        return when (this) {
            InternalObjCValueType.BOOL -> ObjCValueType.BOOL
            InternalObjCValueType.UNICHAR -> ObjCValueType.UNICHAR
            InternalObjCValueType.CHAR -> ObjCValueType.CHAR
            InternalObjCValueType.SHORT -> ObjCValueType.SHORT
            InternalObjCValueType.INT -> ObjCValueType.INT
            InternalObjCValueType.LONG_LONG -> ObjCValueType.LONG_LONG
            InternalObjCValueType.UNSIGNED_CHAR -> ObjCValueType.UNSIGNED_CHAR
            InternalObjCValueType.UNSIGNED_SHORT -> ObjCValueType.UNSIGNED_SHORT
            InternalObjCValueType.UNSIGNED_INT -> ObjCValueType.UNSIGNED_INT
            InternalObjCValueType.UNSIGNED_LONG_LONG -> ObjCValueType.UNSIGNED_LONG_LONG
            InternalObjCValueType.FLOAT -> ObjCValueType.FLOAT
            InternalObjCValueType.DOUBLE -> ObjCValueType.DOUBLE
            InternalObjCValueType.POINTER -> ObjCValueType.POINTER
        }
    }
}
