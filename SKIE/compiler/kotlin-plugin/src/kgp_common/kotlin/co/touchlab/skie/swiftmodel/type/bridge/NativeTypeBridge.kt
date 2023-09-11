package co.touchlab.skie.swiftmodel.type.bridge

import co.touchlab.skie.swiftmodel.type.translation.ObjCValueType

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
