package co.touchlab.skie.plugin.api.model.type

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.types.KotlinType

sealed interface NativeKotlinType {
    data class Nullable(val type: NativeKotlinType) : NativeKotlinType

    data class BlockPointer(val parameterTypes: List<NativeKotlinType>, val returnType: NativeKotlinType) : NativeKotlinType

    sealed interface Reference : NativeKotlinType {
        sealed interface Known : Reference {
            object String : Known
            object Unit : Known

            object Nothing : Known
            sealed interface Array : Known {
                data class Primitive(val elementType: PrimitiveType) : Array
                data class Generic(val elementType: NativeKotlinType) : Array
            }

            data class List(val elementType: NativeKotlinType) : Known
            data class MutableList(val elementType: NativeKotlinType) : Known
            data class Set(val elementType: NativeKotlinType) : Known
            data class MutableSet(val elementType: NativeKotlinType) : Known
            data class Map(val keyType: NativeKotlinType, val valueType: NativeKotlinType) : Known
            data class MutableMap(val keyType: NativeKotlinType, val valueType: NativeKotlinType) : Known

            data class SuspendFunction(
                val kotlinType: KotlinType,
                val descriptor: ClassDescriptor,
                val parameterTypes: kotlin.collections.List<NativeKotlinType>,
                val returnType: NativeKotlinType,
            ) : Known
        }

        data class TypeParameter(val name: String, val upperBound: NativeKotlinType) : Reference

        data class Unknown(val kotlinType: KotlinType, val descriptor: ClassDescriptor) : Reference
    }

    enum class Value : NativeKotlinType {
        BOOL,
        CHAR,
        SHORT,
        LONG_LONG,
        INT,
        UNSIGNED_CHAR,
        UNSIGNED_SHORT,
        UNSIGNED_INT,
        UNSIGNED_LONG_LONG,
        FLOAT,
        DOUBLE,
    }

    object Unichar : NativeKotlinType

    sealed interface Pointer : NativeKotlinType {
        object NativePtr : Pointer

        object Other : Pointer
    }

    object Any : NativeKotlinType
}
