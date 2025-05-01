package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.oir.type.VoidOirType
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType
import org.jetbrains.kotlin.types.KotlinType

actual fun ObjCValueType.mapToOir(kotlinType: KotlinType): OirType = when (this) {
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
    ObjCValueType.VECTOR_FLOAT_128 -> PrimitiveOirType.vectorFloat128
}
