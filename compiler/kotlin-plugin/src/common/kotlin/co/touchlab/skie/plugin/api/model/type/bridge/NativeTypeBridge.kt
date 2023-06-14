package co.touchlab.skie.plugin.api.model.type.bridge

import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType

sealed class NativeTypeBridge {
    object Reference : NativeTypeBridge()

    data class BlockPointer(
        val numberOfParameters: Int,
        val returnsVoid: Boolean,
    ) : NativeTypeBridge()

    data class ValueType(
        val objCValueType: ObjCValueType,
    ) : NativeTypeBridge()
}
