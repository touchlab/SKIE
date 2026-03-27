package co.touchlab.skie.test

import co.touchlab.skie.KotlinCompilerVersion
import co.touchlab.skie.kotlingenerator.ir.KotlinType
import co.touchlab.skie.kotlingenerator.ir.KotlinTypeParameter
import co.touchlab.skie.kotlingenerator.ir.isOptional
import co.touchlab.skie.kotlingenerator.ir.nestedClass
import co.touchlab.skie.kotlingenerator.ir.parameterizedBy

object TestedType {

    interface EnumWithKotlinTypes {

        val kotlinType: KotlinType
    }

    enum class Primitive(packageName: String, className: String) : EnumWithKotlinTypes {

        Boolean("kotlin", "Boolean"),
        Byte("kotlin", "Byte"),
        UByte("kotlin", "UByte"),
        Short("kotlin", "Short"),
        UShort("kotlin", "UShort"),
        Int("kotlin", "Int"),
        UInt("kotlin", "UInt"),
        Long("kotlin", "Long"),
        ULong("kotlin", "ULong"),
        Float("kotlin", "Float"),
        Double("kotlin", "Double"),
        Char("kotlin", "Char"),
        VectorFloat128("kotlinx.cinterop", "Vector128")
        ;

        override val kotlinType: KotlinType = KotlinType.Declared(packageName, className, name)
    }

    enum class Builtin(packageName: String, className: String) : EnumWithKotlinTypes {

        Unit("kotlin", "Unit"),
        Nothing("kotlin", "Nothing"),
        CharSequence("kotlin", "CharSequence"),
        Number("kotlin", "Number"),
        String("kotlin", "String"),
        Any("kotlin", "Any"),
        COpaquePointer("kotlinx.cinterop", "COpaquePointer"),
        NativePtr("kotlin.native.internal", "NativePtr"),
        NativePointed("kotlinx.cinterop", "NativePointed"),
        ObjCProtocol("kotlinx.cinterop", "ObjCProtocol"),
        ;

        override val kotlinType: KotlinType = KotlinType.Declared(packageName, className)
    }

    fun platform(module: String, name: String): KotlinType =
        KotlinType.Declared("platform.$module", name, "${module}__$name")

    enum class PrimitiveArray(packageName: String, className: String) : EnumWithKotlinTypes {

        BooleanArray("kotlin", "BooleanArray"),
        ByteArray("kotlin", "ByteArray"),
        UByteArray("kotlin", "UByteArray"),
        ShortArray("kotlin", "ShortArray"),
        UShortArray("kotlin", "UShortArray"),
        IntArray("kotlin", "IntArray"),
        UIntArray("kotlin", "UIntArray"),
        LongArray("kotlin", "LongArray"),
        ULongArray("kotlin", "ULongArray"),
        FloatArray("kotlin", "FloatArray"),
        DoubleArray("kotlin", "DoubleArray"),
        CharArray("kotlin", "CharArray")
        ;

        override val kotlinType: KotlinType = KotlinType.Declared(packageName, className)
    }

    object CPointed {

        val cFunction = KotlinType.Declared("kotlinx.cinterop", "CFunction").parameterizedBy(KotlinType.Star)

        enum class COpaque(packageName: String, className: String) : EnumWithKotlinTypes {
            // FIXME? `clang` and `llvm` are not available for the tested Kotlin file
            // CXTargetInfoImpl("clang.CXTargetInfoImpl"),
            ;

            override val kotlinType: KotlinType = KotlinType.Declared(packageName, className)
        }

        object CVariable {

            fun cPointerVarOf(type: KotlinType): KotlinType =
                KotlinType.Declared("kotlinx.cinterop", "CPointerVarOf").parameterizedBy(type)

            enum class CPrimitiveVar : EnumWithKotlinTypes {

                Boolean,
                Byte,
                Int,
                UInt,
                ;

                override val kotlinType: KotlinType = KotlinType.Declared("kotlinx.cinterop", "${name}VarOf")
                    .parameterizedBy(KotlinType.Declared("kotlin", name))
            }

            enum class CEnumVar(packageName: String, className: String) : EnumWithKotlinTypes {

                NSProcessInfoThermalState("platform.Foundation", "NSProcessInfoThermalState.Var"),
                NSTimeZoneNameStyle("platform.Foundation", "NSTimeZoneNameStyle.Var"),
                ;

                override val kotlinType: KotlinType = KotlinType.Declared(packageName, className, name)
            }

            enum class CStructVar(override val kotlinType: KotlinType) : EnumWithKotlinTypes {

                NSDecimal(platform("Foundation", "NSDecimal")),
                CGRect(platform("CoreGraphics", "CGRect")),
                CGSize(platform("CoreGraphics", "CGSize")),
                CGPoint(platform("CoreGraphics", "CGPoint")),
                ;
            }
        }
    }

    fun singleTypeParamClass(typeParameter: KotlinType): KotlinType =
        KotlinType.Declared("co.touchlab.skie.test", "SingleTypeParamClass").parameterizedBy(typeParameter)

    fun singleAnyTypeParamClass(typeParameter: KotlinType): KotlinType =
        KotlinType.Declared("co.touchlab.skie.test", "SingleAnyTypeParamClass").parameterizedBy(typeParameter)

    fun recursiveGenericsInterface(typeParameter: KotlinType?): KotlinType =
        KotlinType.Declared("co.touchlab.skie.test", "RecursiveGenericsInterface").parameterizedBy(typeParameter ?: KotlinType.Star)

    fun exportedTypes(typeParameter: KotlinType): List<KotlinType> {
        fun exported(name: String) = KotlinType.Declared("co.touchlab.skie.test.exported", name)

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
            },
            exported("ExportedEnum").let {
                listOf(
                    it,
                    it.nestedClass("NestedClassInEnum"),
                    it.nestedClass("NestedEnumInEnum"),
                    it.nestedClass("NestedObjectInEnum"),
                    it.nestedClass("NestedInterfaceInEnum"),
                    it.nestedClass("Companion"),
                )
            },
            listOf(exported("ExportedEmptyEnum")),
            exported("ExportedInterface").let {
                listOf(
                    it,
                    it.nestedClass("NestedClassInInterface"),
                    it.nestedClass("NestedEnumInInterface"),
                    it.nestedClass("NestedObjectInInterface"),
                    it.nestedClass("NestedInterfaceInInterface"),
                    it.nestedClass("Companion"),
                )
            },
            exported("ExportedObject").let {
                listOf(
                    it,
                    it.nestedClass("NestedClassInObject"),
                    it.nestedClass("NestedEnumInObject"),
                    it.nestedClass("NestedObjectInObject"),
                    it.nestedClass("NestedInterfaceInObject"),
                )
            },
            exported("ExportedSingleParamInterface").let {
                listOf(
                    it.parameterizedBy(typeParameter),
                    it.nestedClass("NestedClassInGenericInterface"),
                    it.nestedClass("NestedEnumInGenericInterface"),
                    it.nestedClass("NestedObjectInGenericInterface"),
                    it.nestedClass("NestedInterfaceInGenericInterface"),
                    it.nestedClass("Companion"),
                )
            },
            exported("ExportedSingleParamClass").let {
                listOf(
                    it.parameterizedBy(typeParameter),
                    it.nestedClass("NestedClassInGenericClass"),
                    it.nestedClass("NestedEnumInGenericClass"),
                    it.nestedClass("NestedObjectInGenericClass"),
                    it.nestedClass("NestedInterfaceInGenericClass"),
                    it.nestedClass("Companion"),
                )
            },
        ).flatten()
    }

    fun nonexportedTypes(typeParameter: KotlinType): List<KotlinType> {
        fun nonexported(name: String) = KotlinType.Declared("co.touchlab.skie.test.nonexported", name)

        return listOf(
            nonexported("NonexportedClass"),
            nonexported("NonexportedEnum"),
            nonexported("NonexportedEmptyEnum"),
            nonexported("NonexportedInterface"),
            nonexported("NonexportedObject"),
            nonexported("NonexportedSingleParamClass").parameterizedBy(typeParameter),
        )
    }

    fun array(element: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "Array").parameterizedBy(element)

    fun list(element: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "List").parameterizedBy(element)

    fun mutableList(element: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "MutableList").parameterizedBy(element)

    fun set(element: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "Set").parameterizedBy(element)

    fun mutableSet(element: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "MutableSet").parameterizedBy(element)

    fun map(key: KotlinType, value: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "Map").parameterizedBy(key, value)

    fun mutableMap(key: KotlinType, value: KotlinType): KotlinType =
        KotlinType.Declared("kotlin", "MutableMap").parameterizedBy(key, value)

    fun cPointer(type: KotlinType): KotlinType =
        KotlinType.Declared("kotlinx.cinterop", "CPointer").parameterizedBy(type)

    val CPOINTEDS: List<KotlinType> by lazy {
        listOf(
            CPointed.cFunction,
        ) + (CPointed.COpaque.entries +
            CPointed.CVariable.CPrimitiveVar.entries +
            CPointed.CVariable.CEnumVar.entries +
            CPointed.CVariable.CStructVar.entries).map { it.kotlinType }
    }

    val CPOINTERS_FIRST_LEVEL: List<KotlinType> by lazy {
        CPOINTEDS.map {
            cPointer(it)
        }
    }

    val CPOINTERS_SECOND_LEVEL: List<KotlinType> by lazy {
        CPOINTERS_FIRST_LEVEL.map {
            cPointer(CPointed.CVariable.cPointerVarOf(it))
        }
    }

    val PLATFORM_TYPES: List<KotlinType> by lazy {
        listOf(
            platform("Foundation", "NSString"),
            platform("Foundation", "NSArray"),
            platform("Foundation", "NSMutableArray"),
            platform("Foundation", "NSSet"),
            platform("Foundation", "NSMutableSet"),
            platform("Foundation", "NSDictionary"),
            platform("Foundation", "NSMutableDictionary"),
            platform("Foundation", "NSValue"),
            platform("Foundation", "NSNumber"),
            platform("Foundation", "NSURLSession"),
        )
    }

    val SUPPORTED_PRIMITIVE_TYPES: Set<Primitive> =
        if (KotlinCompilerVersion.current <= KotlinCompilerVersion.`2_0_0`) {
            Primitive.entries.toSet() - Primitive.VectorFloat128
        } else {
            Primitive.entries.toSet()
        }

    val BASIC: List<KotlinType> by lazy {
        listOf<Collection<EnumWithKotlinTypes>>(
            SUPPORTED_PRIMITIVE_TYPES,
            PrimitiveArray.entries,
            Builtin.entries,
        ).flatten().map { it.kotlinType } + PLATFORM_TYPES
    }

    val CLASS_TYPE_SPECIFIC_BOUNDS: List<KotlinType> by lazy {
        (BASIC.map(KotlinType::Optional) + listOf(
            list(KotlinType.Optional(Builtin.String.kotlinType)),
            list(list(Builtin.String.kotlinType)),
            KotlinType.Optional(Builtin.String.kotlinType),
        ) + level(listOf(Builtin.String.kotlinType)) + exportedTypes(Builtin.String.kotlinType) + exportedTypes(Primitive.Int.kotlinType).map(KotlinType::Optional))
            .filter {
                it !is KotlinType.Lambda || it.receiverType == null
            }
            .filter(ENABLED_FILTER)
            .toSet()
            .sortedBy { it.getSafeName() }
    }

    val CLASS_TYPE_PARAMS: List<KotlinType> by lazy {
        (BASIC + CLASS_TYPE_SPECIFIC_BOUNDS)
            .filter { it != Builtin.Nothing.kotlinType }
            .map { KotlinTypeParameter(null, it).toUsage() }
    }

    val FIRST_LEVEL: List<KotlinType> by lazy {
        level(BASIC + CPOINTEDS + CLASS_TYPE_PARAMS) + CPOINTERS_FIRST_LEVEL
    }

    val SECOND_LEVEL: List<KotlinType> by lazy {
        level(FIRST_LEVEL) + CPOINTERS_SECOND_LEVEL
    }

    val SPECIFIC: List<KotlinType> by lazy {
        listOf(
            list(KotlinType.Optional(singleTypeParamClass(Builtin.String.kotlinType))),
            list(KotlinType.Optional(singleAnyTypeParamClass(Builtin.String.kotlinType))),
            list(
                KotlinType.Lambda(
                    false,
                    null,
                    listOf(KotlinType.Lambda(false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType)),
                    Builtin.String.kotlinType,
                ),
            ),
            list(KotlinType.Optional(KotlinType.Lambda(false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType))),
            KotlinType.Optional(
                KotlinType.Lambda(false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType),
            ),
            list(KotlinType.Star),
            singleTypeParamClass(KotlinType.Star),
            singleAnyTypeParamClass(KotlinType.Star),
            map(KotlinType.Star, KotlinType.Star),
            recursiveGenericsInterface(KotlinType.Star),
            KotlinTypeParameter {
                listOf(
                    recursiveGenericsInterface(it),
                )
            }.toUsage(),
        )
    }

    val ONLY: List<KotlinType> by lazy {
//         fun LL(type: KotlinType) = KotlinType.Lambda(
//             false,
//             null,
//             listOf(
//                 KotlinType.Lambda(
//                     false,
//                     null,
//                     listOf(type),
//                     type,
//                 ),
//             ),
//             KotlinType.Lambda(
//                 false,
//                 null,
//                 listOf(type),
//                 type,
//             ),
//         )
//         fun LN(type: KotlinType) = KotlinType.Lambda(
//             false,
//             null,
//             listOf(KotlinType.Optional(type)),
//             KotlinType.Optional(type),
//         )
//
//         listOf<KotlinType>(
//             LL(Builtin.Nothing.kotlinType),
//             LL(Builtin.Unit.kotlinType),
//         ) + CPOINTEDS.map {
//             LN(it)
//         } +
        listOf(
//             Builtin.NativePtr.kotlinType,
//             Builtin.NativePtr.kotlinType,
//             list(Builtin.NativePtr.kotlinType),
//             KotlinType.Lambda(
//                 false,
//                 null,
//                 listOf(Builtin.NativePtr.kotlinType),
//                 Builtin.NativePtr.kotlinType,
//             ),
//             KotlinType.Optional(mutableSet(KotlinTypeParameter(KotlinType.Lambda(
//                 false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType,
//             )).toUsage())),
//
//             KotlinType.Lambda(
//                 false,
//                 null,
//                 listOf(
//                     array(
//                         KotlinTypeParameter(KotlinType.Lambda(
//                             false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType,
//                         )).toUsage()
//                     )
//                 ),
//                 array(
//                     KotlinTypeParameter(KotlinType.Lambda(
//                         false, null, listOf(Builtin.String.kotlinType), Builtin.String.kotlinType,
//                     )).toUsage()
//                 )
//             )
//             Builtin.String.kotlinType
//             singleTypeParamClass(Builtin.String.kotlinType),
//
//             KotlinType.Lambda(
//                 false,
//                 null,
//                 listOf(
//                     Builtin.String.kotlinType,
//                 ),
//                 Builtin.String.kotlinType,
//             ),

//             suspend_function_lambda_Map_Boolean_NULLABLE_CHAR_Map_Boolean_NULLABLE_CHAR
        ) // + CLASS_TYPE_PARAMS + level(CLASS_TYPE_PARAMS) +

        // PLATFORM_TYPES
        // BASIC
    }

    val ENABLED_FILTER: (KotlinType) -> Boolean = { outerType ->
        fun hasLambdaTypeParam(type: KotlinType, visitedTypeParameters: List<KotlinTypeParameter>): Boolean = when (type) {
            is KotlinType.Parametrized -> type.typeArguments.any { it is KotlinType.Lambda || hasLambdaTypeParam(it, visitedTypeParameters) }
            is KotlinType.TypeParameterUsage -> {
                if (type.typeParameter !in visitedTypeParameters) {
                    type.typeParameter.bounds.any {
                        it is KotlinType.Lambda || hasLambdaTypeParam(it, visitedTypeParameters + type.typeParameter)
                    }
                } else {
                    false
                }
            }
            is KotlinType.Optional -> type.wrapped is KotlinType.Lambda || hasLambdaTypeParam(type.wrapped, visitedTypeParameters)
            is KotlinType.Lambda -> {
                hasLambdaTypeParam(type.returnType, visitedTypeParameters) || type.parameterTypes.any { hasLambdaTypeParam(it, visitedTypeParameters) }
            }
            else -> false
        }

        !hasLambdaTypeParam(outerType, emptyList())
    }

    val ALL_BUT_SECOND_LEVEL: List<KotlinType> by lazy {
        ONLY.ifEmpty {
            (BASIC + CLASS_TYPE_PARAMS + FIRST_LEVEL + SPECIFIC)
        }.filter(ENABLED_FILTER).toSet().sortedBy { it.getSafeName() }
    }

    val ALL: List<KotlinType> by lazy {
        ONLY.ifEmpty {
            (BASIC + CLASS_TYPE_PARAMS + FIRST_LEVEL + SECOND_LEVEL + SPECIFIC)
        }.filter(ENABLED_FILTER).toSet().sortedBy { it.getSafeName() }
    }

    private fun level(types: List<KotlinType>): List<KotlinType> {
        return types.flatMap { type ->
            listOfNotNull(
                KotlinType.Optional(type),
                singleTypeParamClass(type),
                singleAnyTypeParamClass(type).takeIf { !type.isOptional },
                array(type).takeIf {
                    when (type) {
                        Builtin.Nothing.kotlinType -> false
                        is KotlinType.Optional -> type.wrapped != Builtin.Nothing.kotlinType
                        else -> true
                    }
                },
                list(type),
                mutableList(type),
                set(type),
                mutableSet(type),
                KotlinType.Lambda(
                    isSuspend = false,
                    receiverType = null,
                    parameterTypes = listOf(type),
                    returnType = type,
                ),
                KotlinType.Lambda(
                    isSuspend = true,
                    receiverType = null,
                    parameterTypes = listOf(type),
                    returnType = type,
                ),
                KotlinType.Lambda(
                    isSuspend = false,
                    receiverType = type,
                    parameterTypes = listOf(),
                    returnType = type,
                ),
                KotlinType.Lambda(
                    isSuspend = true,
                    receiverType = type,
                    parameterTypes = listOf(),
                    returnType = type,
                ),
            ) + exportedTypes(type) + nonexportedTypes(type)
        } + types.flatMap { key ->
            val value = types.first()
            listOf(
                map(key, value),
                mutableMap(key, value),
            )
        } + types.flatMap { value ->
            val key = types.first()
            listOf(
                map(key, value),
                mutableMap(key, value),
            )
        }
    }
}
