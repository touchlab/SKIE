package co.touchlab.skie.test

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.CHAR_SEQUENCE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MUTABLE_SET
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.U_BYTE
import com.squareup.kotlinpoet.U_BYTE_ARRAY
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.U_INT_ARRAY
import com.squareup.kotlinpoet.U_LONG
import com.squareup.kotlinpoet.U_LONG_ARRAY
import com.squareup.kotlinpoet.U_SHORT
import com.squareup.kotlinpoet.U_SHORT_ARRAY

fun TestedType.copy(
    safeName: String = this.safeName,
    kotlinType: TypeName = this.kotlinType,
): TestedType = TestedType.CopiedType(
    safeName = safeName,
    kotlinType = kotlinType,
)

sealed interface TestedType {
    val safeName: String
    val kotlinType: TypeName

    enum class Primitive(
        override val kotlinType: TypeName,
    ): TestedType {
        Boolean(BOOLEAN),
        Byte(BYTE),
        UByte(U_BYTE),
        Short(SHORT),
        UShort(U_SHORT),
        Int(INT),
        UInt(U_INT),
        Long(LONG),
        ULong(U_LONG),
        Float(FLOAT),
        Double(DOUBLE),
        Char(CHAR);

        override val safeName: kotlin.String
            get() = name
    }

    enum class Builtin(
        override val kotlinType: TypeName,
    ): TestedType {
        Unit(UNIT),
        Nothing(NOTHING),
        CharSequence(CHAR_SEQUENCE),
        Number(NUMBER),
        String(STRING),
        Any(ANY),
        ;

        override val safeName: kotlin.String
            get() = name
    }

    enum class PrimitiveArray(
        override val kotlinType: TypeName,
    ): TestedType {
        BooleanArray(BOOLEAN_ARRAY),
        ByteArray(BYTE_ARRAY),
        UByteArray(U_BYTE_ARRAY),
        ShortArray(SHORT_ARRAY),
        UShortArray(U_SHORT_ARRAY),
        IntArray(INT_ARRAY),
        UIntArray(U_INT_ARRAY),
        LongArray(LONG_ARRAY),
        ULongArray(U_LONG_ARRAY),
        FloatArray(FLOAT_ARRAY),
        DoubleArray(DOUBLE_ARRAY),
        CharArray(CHAR_ARRAY);

        override val safeName: String get() = name
    }

    data class Nullable(
        val wrapped: TestedType
    ): TestedType {
        override val safeName: String get() = "nullable_${wrapped.safeName}"
        override val kotlinType: TypeName get() = wrapped.kotlinType.copy(nullable = true)
    }

    data class Basic(
        val type: ClassName,
    ): TestedType {
        override val safeName: String get() = type.simpleName
        override val kotlinType: TypeName get() = type
    }

    data class WithTypeParameters(
        val type: ClassName,
        val typeParameters: List<TestedType>,
    ): TestedType {
        override val safeName: String get() = type.simpleName + typeParameters.joinToString(
            prefix = "_",
            separator = "_",
            transform = { it.safeName }
        )
        override val kotlinType: TypeName get() = type.parameterizedBy(typeParameters.map { it.kotlinType })
    }

    object Star: TestedType {
        override val safeName: String get() = "star"
        override val kotlinType: TypeName get() = STAR
    }

    data class Lambda(
        val isSuspend: Boolean,
        val receiverType: TestedType?,
        val parameterTypes: List<TestedType>,
        val returnType: TestedType,
    ): TestedType {
        override val safeName: String get() = (
            listOfNotNull("suspend".takeIf { isSuspend }, "lambda", receiverType?.let { "on__${it.safeName}_" }) +
            parameterTypes.map { it.safeName } +
            listOf(returnType.safeName)
        ).joinToString("_")

        override val kotlinType: TypeName
            get() = LambdaTypeName.get(
                receiver = receiverType?.kotlinType,
                parameters = parameterTypes.map { ParameterSpec.unnamed(it.kotlinType) },
                returnType = returnType.kotlinType,
            ).copy(suspending = isSuspend)
    }

    data class CopiedType(
        override val safeName: String,
        override val kotlinType: TypeName,
    ): TestedType

    companion object {
        fun SingleTypeParamClass(param: TestedType) = WithTypeParameters(
            type = ClassName("co.touchlab.skie.test", "SingleTypeParamClass"),
            typeParameters = listOf(param),
        )

        fun ExportedTypes(param: TestedType): List<TestedType> {
            fun exported(name: String) = ClassName("co.touchlab.skie.test.exported", name)
            return listOf(
                Basic(exported("ExportedClass")),
                Basic(exported("ExportedEnum")),
                Basic(exported("ExportedInterface")),
                Basic(exported("ExportedObject")),
                WithTypeParameters(
                    type = exported("ExportedSingleParamClass"),
                    typeParameters = listOf(param),
                ),
            )
        }

        fun NonexportedTypes(param: TestedType): List<TestedType> {
            fun nonexported(name: String) = ClassName("co.touchlab.skie.test.nonexported", name)
            return listOf(
                Basic(nonexported("NonexportedClass")),
                Basic(nonexported("NonexportedEnum")),
                Basic(nonexported("NonexportedInterface")),
                Basic(nonexported("NonexportedObject")),
                WithTypeParameters(
                    type = nonexported("NonexportedSingleParamClass"),
                    typeParameters = listOf(param),
                ),
            )
        }

        fun Array(element: TestedType) = WithTypeParameters(
            type = ARRAY,
            typeParameters = listOf(element),
        )

        fun List(element: TestedType) = WithTypeParameters(
            type = LIST,
            typeParameters = listOf(element),
        )

        fun MutableList(element: TestedType) = WithTypeParameters(
            type = MUTABLE_LIST,
            typeParameters = listOf(element),
        )

        fun Set(element: TestedType) = WithTypeParameters(
            type = SET,
            typeParameters = listOf(element),
        )

        fun MutableSet(element: TestedType) = WithTypeParameters(
            type = MUTABLE_SET,
            typeParameters = listOf(element),
        )

        fun Map(key: TestedType, value: TestedType) = WithTypeParameters(
            type = MAP,
            typeParameters = listOf(key, value),
        )

        fun MutableMap(key: TestedType, value: TestedType) = WithTypeParameters(
            type = MUTABLE_MAP,
            typeParameters = listOf(key, value),
        )

        val BASIC by lazy<List<TestedType>> {
            listOf(Primitive.values(), PrimitiveArray.values(), Builtin.values()).flatMap { it.toList() }
        }

        val FIRST_LEVEL by lazy {
            level(BASIC)
        }

        val SECOND_LEVEL by lazy {
            level(FIRST_LEVEL)
        }

        val SPECIFIC by lazy {
            listOf(
                List(Nullable(SingleTypeParamClass(Builtin.String))),
                List(Lambda(false, null, listOf(Lambda(false, null, listOf(Builtin.String), Builtin.String)), Builtin.String)),
                List(Nullable(Lambda(false, null, listOf(Builtin.String), Builtin.String))),
                Nullable(Lambda(false, null, listOf(Builtin.String), Builtin.String)),
                List(Star),
                SingleTypeParamClass(Star),
                Map(Star, Star),
            )
        }

        val ONLY by lazy<List<TestedType>> {
            listOf(
                // Lambda(false,
                //     Lambda(false, null, listOf(TestedType.Builtin.Any), TestedType.Builtin.Any),
                //     listOf(),
                //     Lambda(false, null, listOf(TestedType.Builtin.Any), TestedType.Builtin.Any),
                // ),
            )
        }

        val ENABLED_FILTER: (TestedType) -> Boolean = {
            fun hasLambdaTypeParam(type: TestedType): Boolean = when (type) {
                is WithTypeParameters -> type.typeParameters.any { it is Lambda || hasLambdaTypeParam(it) }
                else -> false
            }

            !hasLambdaTypeParam(it)
        }

        val ALL_BUT_SECOND_LEVEL by lazy {
            ONLY.ifEmpty {
                (BASIC + FIRST_LEVEL + SPECIFIC).toSet().sortedBy { it.safeName }.filter(ENABLED_FILTER)
            }.filter(ENABLED_FILTER)
        }

        val ALL by lazy {
            ONLY.ifEmpty {
                (BASIC + FIRST_LEVEL + SECOND_LEVEL + SPECIFIC).toSet().sortedBy { it.safeName }.filter(ENABLED_FILTER)
            }
        }

        private fun level(types: List<TestedType>): List<TestedType> {
            return types.flatMap { type ->
                listOfNotNull(
                    Nullable(type),
                    SingleTypeParamClass(type),
                    Array(type).takeIf {
                        when (type) {
                            Builtin.Nothing -> false
                            is Nullable -> type.wrapped != Builtin.Nothing
                            else -> true
                        }
                    },
                    List(type),
                    MutableList(type),
                    Set(type),
                    MutableSet(type),
                    Lambda(
                        isSuspend = false,
                        receiverType = null,
                        parameterTypes = listOf(type),
                        returnType = type,
                    ),
                    Lambda(
                        isSuspend = true,
                        receiverType = null,
                        parameterTypes = listOf(type),
                        returnType = type,
                    ),
                    Lambda(
                        isSuspend = false,
                        receiverType = type,
                        parameterTypes = listOf(),
                        returnType = type,
                    ),
                    Lambda(
                        isSuspend = true,
                        receiverType = type,
                        parameterTypes = listOf(),
                        returnType = type,
                    ),
                ) + ExportedTypes(type) + NonexportedTypes(type)
            } + types.flatMap { key ->
                val value = types.first()
                listOf(
                    Map(key, value),
                    MutableMap(key, value),
                )
            } + types.flatMap { value ->
                val key = types.first()
                listOf(
                    Map(key, value),
                    MutableMap(key, value),
                )
            }
        }
    }
}
