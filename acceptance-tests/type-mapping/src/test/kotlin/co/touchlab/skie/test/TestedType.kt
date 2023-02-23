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
import com.squareup.kotlinpoet.TypeVariableName
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
        COpaquePointer(ClassName("kotlinx.cinterop", "COpaquePointer")),
        NativePtr(ClassName("kotlin.native.internal", "NativePtr")),
        NativePointed(ClassName("kotlinx.cinterop", "NativePointed")),
        ;

        override val safeName: kotlin.String
            get() = name
    }

    data class Platform(
        val module: String,
        val typeName: String,
    ): TestedType {

        override val kotlinType: TypeName
            get() = ClassName("platform.$module", typeName)

        override val safeName: String
            get() = "${module}__$typeName"
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

    sealed interface CPointed: TestedType {
        object CFunction: CPointed {
            override val safeName: String = "CFunction"
            override val kotlinType: TypeName = ClassName("kotlinx.cinterop", "CFunction")
                .parameterizedBy(STAR)
        }

        enum class COpaque(override val kotlinType: TypeName): CPointed {
            // FIXME? `clang` and `llvm` are not available for the tested Kotlin file
            // CXTargetInfoImpl(ClassName("clang", "CXTargetInfoImpl")),
            ;

            override val safeName: String
                get() = name
        }

        sealed interface CVariable: CPointed {
            class CPointerVarOf(type: TestedType): CVariable {
                override val safeName: String = "CPointerVarOf__${type.safeName}__"
                override val kotlinType: TypeName = ClassName("kotlinx.cinterop", "CPointerVarOf").parameterizedBy(type.kotlinType)
            }

            enum class CPrimitiveVar: CVariable {
                Boolean,
                Byte,
                Int,
                UInt,
                ;

                override val safeName: String
                    get() = "${name}VarOf"

                override val kotlinType: TypeName
                    get() = ClassName("kotlinx.cinterop", safeName)
                        .parameterizedBy(ClassName("kotlin", name))
            }

            enum class CEnumVar(override val kotlinType: TypeName): CVariable {
                NSProcessInfoThermalState(ClassName("platform.Foundation", "NSProcessInfoThermalState", "Var")),
                NSTimeZoneNameStyle(ClassName("platform.Foundation", "NSTimeZoneNameStyle", "Var")),
                ;

                override val safeName: String
                    get() = name
            }

            enum class CStructVar(override val kotlinType: TypeName): CVariable {
                NSDecimal(ClassName("platform.Foundation", "NSDecimal")),
                CGRect(ClassName("platform.CoreGraphics", "CGRect")),
                CGSize(ClassName("platform.CoreGraphics", "CGSize")),
                CGPoint(ClassName("platform.CoreGraphics", "CGPoint")),
                ;

                override val safeName: String
                    get() = name
            }
        }
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
        override val safeName: String get() = type.simpleNames.joinToString("___")
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

    data class TypeParam(val bounds: List<TestedType>): TestedType {
        override val safeName: String
            get() = bounds.joinToString("_") { it.safeName.uppercase() }

        override val kotlinType: TypeVariableName
            get() = TypeVariableName(safeName, bounds.map { it.kotlinType })
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
                exported("ExportedClass").let {
                    listOf(
                        it,
                        it.nestedClass("NestedClassInClass"),
                        it.nestedClass("NestedEnumInClass"),
                        it.nestedClass("NestedObjectInClass"),
                        it.nestedClass("NestedInterfaceInClass"),
                        it.nestedClass("Companion"),
                    )
                }.map(::Basic),
                exported("ExportedEnum").let {
                    listOf(
                        it,
                        it.nestedClass("NestedClassInEnum"),
                        it.nestedClass("NestedEnumInEnum"),
                        it.nestedClass("NestedObjectInEnum"),
                        it.nestedClass("NestedInterfaceInEnum"),
                        it.nestedClass("Companion"),
                    )
                }.map(::Basic),
                listOf(Basic(exported("ExportedEmptyEnum"))),
                exported("ExportedInterface").let {
                    listOf(
                        it,
                        it.nestedClass("NestedClassInInterface"),
                        it.nestedClass("NestedEnumInInterface"),
                        it.nestedClass("NestedObjectInInterface"),
                        it.nestedClass("NestedInterfaceInInterface"),
                        it.nestedClass("Companion"),
                    )
                }.map(::Basic),
                exported("ExportedObject").let {
                    listOf(
                        it,
                        it.nestedClass("NestedClassInObject"),
                        it.nestedClass("NestedEnumInObject"),
                        it.nestedClass("NestedObjectInObject"),
                        it.nestedClass("NestedInterfaceInObject"),
                    )
                }.map(::Basic),
                WithTypeParameters(
                    type = exported("ExportedSingleParamInterface"),
                    typeParameters = listOf(param),
                ).let {
                    listOf(it) + listOf(
                        it.type.nestedClass("NestedClassInGenericInterface"),
                        it.type.nestedClass("NestedEnumInGenericInterface"),
                        it.type.nestedClass("NestedObjectInGenericInterface"),
                        it.type.nestedClass("NestedInterfaceInGenericInterface"),
                        it.type.nestedClass("Companion"),
                    ).map(::Basic)
                },
                WithTypeParameters(
                    type = exported("ExportedSingleParamClass"),
                    typeParameters = listOf(param),
                ).let {
                    listOf(it) + listOf(
                        it.type.nestedClass("NestedClassInGenericClass"),
                        it.type.nestedClass("NestedEnumInGenericClass"),
                        it.type.nestedClass("NestedObjectInGenericClass"),
                        it.type.nestedClass("NestedInterfaceInGenericClass"),
                        it.type.nestedClass("Companion"),
                    ).map(::Basic)
                },
            ).flatten()
        }

        fun NonexportedTypes(param: TestedType): List<TestedType> {
            fun nonexported(name: String) = ClassName("co.touchlab.skie.test.nonexported", name)
            return listOf(
                Basic(nonexported("NonexportedClass")),
                Basic(nonexported("NonexportedEnum")),
                Basic(nonexported("NonexportedEmptyEnum")),
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

        fun CPointer(type: CPointed) = WithTypeParameters(
            type = ClassName("kotlinx.cinterop", "CPointer"),
            typeParameters = listOf(type),
        )

        val CPOINTEDS by lazy {
            listOf(
                CPointed.CFunction,
            ) + CPointed.COpaque.values().toList() +
                CPointed.CVariable.CPrimitiveVar.values().toList() +
                CPointed.CVariable.CEnumVar.values().toList() +
                CPointed.CVariable.CStructVar.values().toList()
        }

        val CPOINTERS_FIRST_LEVEL by lazy {
            CPOINTEDS.map {
                CPointer(it)
            }
        }

        val CPOINTERS_SECOND_LEVEL by lazy {
            CPOINTERS_FIRST_LEVEL.map {
                CPointer(CPointed.CVariable.CPointerVarOf(it))
            }
        }

        val PLATFORM_TYPES: List<TestedType> by lazy {
            // TODO: Run `BuiltinSwiftBridgeableProvider` here to get list of all bridged types to try - some might work differently
            listOf(
                Platform("Foundation", "NSString"),
                Platform("Foundation", "NSArray"),
                Platform("Foundation", "NSMutableArray"),
                Platform("Foundation", "NSSet"),
                Platform("Foundation", "NSMutableSet"),
                Platform("Foundation", "NSDictionary"),
                Platform("Foundation", "NSMutableDictionary"),
                Platform("Foundation", "NSValue"),
                Platform("Foundation", "NSNumber"),
            )
        }

        val BASIC by lazy<List<TestedType>> {
            listOf(Primitive.values(), PrimitiveArray.values(), Builtin.values()).flatMap { it.toList() } + PLATFORM_TYPES
        }

        val CLASS_TYPE_SPECIFIC_BOUNDS by lazy<List<TestedType>> {
            (BASIC.map(::Nullable) + listOf(
                List(Nullable(Builtin.String)),
                List(List(Builtin.String)),
                Nullable(List(Builtin.String)),
            ) + level(listOf(Builtin.String)) + ExportedTypes(Builtin.String) + ExportedTypes(Primitive.Int).map(::Nullable))
                .filter {
                    it !is Lambda || it.receiverType == null
                }
                .filter(ENABLED_FILTER)
                .toSet().sortedBy { it.safeName }
        }

        val CLASS_TYPE_PARAMS by lazy<List<TypeParam>> {
            (BASIC + CLASS_TYPE_SPECIFIC_BOUNDS)
                .filter { it != Builtin.Nothing }
                .map { TypeParam(listOf(it)) }
        }

        val FIRST_LEVEL by lazy {
            level(BASIC + CPOINTEDS + CLASS_TYPE_PARAMS) + CPOINTERS_FIRST_LEVEL
        }

        val SECOND_LEVEL by lazy {
            level(FIRST_LEVEL) + CPOINTERS_SECOND_LEVEL
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
            // fun LL(type: TestedType) = Lambda(
            //     false,
            //     null,
            //     listOf(
            //         Lambda(
            //             false,
            //             null,
            //             listOf(type),
            //             type,
            //         ),
            //     ),
            //     Lambda(
            //         false,
            //         null,
            //         listOf(type),
            //         type,
            //     ),
            // )
            // fun LN(type: TestedType) = Lambda(
            //     false,
            //     null,
            //     listOf(Nullable(type)),
            //     Nullable(type),
            // )
            //
            // listOf<TestedType>(
            //     LL(Builtin.Nothing),
            //     LL(Builtin.Unit),
            // ) + CPOINTEDS.map {
            //     LN(it)
            // } +
            listOf<TestedType>(
                // Builtin.NativePtr
                // Lambda(false,
                //     Lambda(false, null, listOf(TypeParam("T1")), TypeParam("T1")),
                //     listOf(),
                //     Lambda(false, null, listOf(TypeParam("T1")), TypeParam("T1")),
                // ),
                // Builtin.NativePtr,
                // List(Builtin.NativePtr),
                // Lambda(
                //     false,
                //     null,
                //     listOf(Builtin.NativePtr),
                //     Builtin.NativePtr,
                // )
                // Nullable(MutableSet(TypeParam(listOf(Lambda(
                //     false, null, listOf(Builtin.String), Builtin.String,
                // )))))

                // Lambda(
                //     false,
                //     null,
                //     listOf(
                //         Array(
                //             TypeParam(listOf(Lambda(
                //                 false, null, listOf(Builtin.String), Builtin.String,
                //             )))
                //         )
                //     ),
                //     Array(
                //         TypeParam(listOf(Lambda(
                //             false, null, listOf(Builtin.String), Builtin.String,
                //         )))
                //     )
                // )
                // Builtin.String
                // SingleTypeParamClass(Builtin.String),

                // Lambda(
                //     false,
                //     null,
                //     listOf(
                //         Builtin.String,
                //     ),
                //     Builtin.String,
                // ),

                // suspend_function_lambda_Map_Boolean_NULLABLE_CHAR_Map_Boolean_NULLABLE_CHAR
            ) //+ CLASS_TYPE_PARAMS + level(CLASS_TYPE_PARAMS) +

            // PLATFORM_TYPES
            // BASIC
        }

        val ENABLED_FILTER: (TestedType) -> Boolean = { outerType ->
            fun hasLambdaTypeParam(type: TestedType): Boolean = when (type) {
                is WithTypeParameters -> type.typeParameters.any { it is Lambda || hasLambdaTypeParam(it) }
                is TypeParam -> type.bounds.any { it is Lambda || hasLambdaTypeParam(it) }
                is Nullable -> type.wrapped is Lambda || hasLambdaTypeParam(type.wrapped)
                is Lambda -> hasLambdaTypeParam(type.returnType) || type.parameterTypes.any { hasLambdaTypeParam(it) }
                else -> false
            }

            !hasLambdaTypeParam(outerType)
        }

        val ALL_BUT_SECOND_LEVEL by lazy {
            ONLY.ifEmpty {
                (BASIC + CLASS_TYPE_PARAMS + FIRST_LEVEL + SPECIFIC)
            }.filter(ENABLED_FILTER).toSet().sortedBy { it.safeName }
        }

        val ALL by lazy {
            ONLY.ifEmpty {
                (BASIC + CLASS_TYPE_PARAMS + FIRST_LEVEL + SECOND_LEVEL + SPECIFIC)
            }.filter(ENABLED_FILTER).toSet().sortedBy { it.safeName }
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
