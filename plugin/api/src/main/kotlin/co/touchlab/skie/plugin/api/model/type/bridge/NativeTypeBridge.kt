package co.touchlab.skie.plugin.api.model.type.bridge

import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.allParameters

sealed class NativeTypeBridge {
    object Reference : NativeTypeBridge()

    data class BlockPointer(
        val numberOfParameters: Int,
        val returnsVoid: Boolean
    ) : NativeTypeBridge()

    data class ValueType(
        val objCValueType: ObjCValueType,
    ) : NativeTypeBridge()
}
