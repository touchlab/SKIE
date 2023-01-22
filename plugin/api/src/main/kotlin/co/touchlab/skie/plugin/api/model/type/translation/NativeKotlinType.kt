package co.touchlab.skie.plugin.api.model.type.translation

import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import io.outfoxx.swiftpoet.DeclaredTypeName
import org.jetbrains.kotlin.backend.konan.llvm.LlvmParameterAttribute
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType

sealed interface SwiftTypeModel: TypeSwiftModel

data class SwiftRawTypeModel(
    val type: DeclaredTypeName,
): SwiftTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = TODO("Not yet implemented")
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

sealed interface SwiftReferenceTypeModel: SwiftTypeModel

sealed interface SwiftNonNullReferenceTypeModel: SwiftReferenceTypeModel

data class SwiftNullableRefefenceTypeModel(
    val nonNullType: SwiftNonNullReferenceTypeModel,
    val isNullableResult: Boolean = false,
): SwiftReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = if (nonNullType is SwiftLambdaTypeModel) {
            "(${nonNullType.stableFqName})?"
        } else {
            "${nonNullType.stableFqName}?"
        }
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

data class SwiftClassTypeModel(
    val className: String,
    val typeArguments: List<SwiftNonNullReferenceTypeModel> = emptyList(),
): SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = if (typeArguments.isEmpty()) {
            className
        } else {
            "$className<${typeArguments.joinToString { it.stableFqName }}>"
        }
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

sealed interface SwiftGenericTypeUsageModel: SwiftNonNullReferenceTypeModel {
    val typeName: String
}

data class SwiftGenericTypeRawUsageModel(
    override val typeName: String,
): SwiftGenericTypeUsageModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = TODO("Not yet implemented")
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

data class SwiftGenericTypeParameterUsageModel(
    val typeParameterDescriptor: TypeParameterDescriptor,
    val namer: ObjCExportNamer,
): SwiftGenericTypeUsageModel {
    override val typeName: String
        get() = namer.getTypeParameterName(typeParameterDescriptor)

    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = typeName
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

data class SwiftProtocolTypeModel(
    val protocolName: String,
): SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = protocolName
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

object SwiftAnyTypeModel: SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "Any"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}


object SwiftAnyObjectTypeModel: SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "AnyObject"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}


object SwiftAnyHashableTypeModel: SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "AnyHashable"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

object SwiftInstanceTypeModel: SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "Self"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

data class SwiftLambdaTypeModel(
    val returnType: SwiftTypeModel,
    val parameterTypes: List<SwiftReferenceTypeModel>,
    val isEscaping: Boolean,
): SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = if (isEscaping) { "@escaping " } else { "" } + "(${parameterTypes.joinToString { it.stableFqName }}) -> ${returnType.stableFqName}"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

object SwiftMetaClassTypeModel: SwiftNonNullReferenceTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = TODO("Not yet implemented")
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

sealed class SwiftPrimitiveTypeModel(
    val name: String,
): SwiftTypeModel {
    object NSUInteger : SwiftPrimitiveTypeModel("NSUInteger")
    object BOOL : SwiftPrimitiveTypeModel("Bool")
    object unichar : SwiftPrimitiveTypeModel("unichar")
    object int8_t : SwiftPrimitiveTypeModel("Int8")
    object int16_t : SwiftPrimitiveTypeModel("Int16")
    object int32_t : SwiftPrimitiveTypeModel("Int32")
    object int64_t : SwiftPrimitiveTypeModel("Int64")
    object uint8_t : SwiftPrimitiveTypeModel("UInt8")
    object uint16_t : SwiftPrimitiveTypeModel("UInt16")
    object uint32_t : SwiftPrimitiveTypeModel("UInt32")
    object uint64_t : SwiftPrimitiveTypeModel("UInt64")
    object float : SwiftPrimitiveTypeModel("Float")
    object double : SwiftPrimitiveTypeModel("Double")
    object NSInteger : SwiftPrimitiveTypeModel("NSInteger")
    object char : SwiftPrimitiveTypeModel("char")
    object unsigned_char: SwiftPrimitiveTypeModel("unsigned char")
    object unsigned_short: SwiftPrimitiveTypeModel("unsigned short")
    object int: SwiftPrimitiveTypeModel("int")
    object unsigned_int: SwiftPrimitiveTypeModel("unsigned int")
    object long: SwiftPrimitiveTypeModel("long")
    object unsigned_long: SwiftPrimitiveTypeModel("unsigned long")
    object long_long: SwiftPrimitiveTypeModel("long long")
    object unsigned_long_long: SwiftPrimitiveTypeModel("unsigned long long")
    object short: SwiftPrimitiveTypeModel("short")

    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = name
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

data class SwiftPointerTypeModel(
    val pointee: SwiftTypeModel,
    val nullable: Boolean = false,
): SwiftTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "UnsafeMutableRawPointer" + if (nullable) "?" else ""
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}

object SwiftVoidTypeModel: SwiftTypeModel {
    override val containingType: TypeSwiftModel?
        get() = TODO("Not yet implemented")
    override val identifier: String
        get() = TODO("Not yet implemented")
    override val stableFqName: String
        get() = "Void"
    override val bridgedOrStableFqName: String
        get() = TODO("Not yet implemented")
    override val isSwiftSymbol: Boolean
        get() = TODO("Not yet implemented")

    override fun fqName(separator: String): String {
        TODO("Not yet implemented")
    }
}


// fun NativeKotlinType.Nullable.swiftModel(context: TypeMappingContext): TypeSwiftModel {
//
// }

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

enum class ObjCValueType(val encoding: String, val defaultParameterAttributes: List<LlvmParameterAttribute> = emptyList()) {
    BOOL("c", listOf(LlvmParameterAttribute.SignExt)),
    UNICHAR("S", listOf(LlvmParameterAttribute.ZeroExt)),
    // TODO: Switch to explicit SIGNED_CHAR
    CHAR("c", listOf(LlvmParameterAttribute.SignExt)),
    SHORT("s", listOf(LlvmParameterAttribute.SignExt)),
    INT("i"),
    LONG_LONG("q"),
    UNSIGNED_CHAR("C", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_SHORT("S", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_INT("I"),
    UNSIGNED_LONG_LONG("Q"),
    FLOAT("f"),
    DOUBLE("d"),
    POINTER("^v")
}
