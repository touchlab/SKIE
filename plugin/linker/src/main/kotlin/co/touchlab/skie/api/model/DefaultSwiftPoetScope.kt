@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.property.reference
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecKind
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.NativeKotlinType
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportMapperReflector
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import io.outfoxx.swiftpoet.ANY
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FLOAT32
import io.outfoxx.swiftpoet.FLOAT64
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.INT16
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64
import io.outfoxx.swiftpoet.INT8
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.UIN16
import io.outfoxx.swiftpoet.UINT32
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.UINT8
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isSuspendFunctionTypeOrSubtype
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isAnyOrNullableAny
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable

internal class DefaultSwiftPoetScope(
    private val swiftModelScope: SwiftModelScope,
    private val namer: ObjCExportNamer,
) : SwiftPoetScope, SwiftModelScope by swiftModelScope {

    override val KotlinType.native: NativeKotlinType
        get() {
            if (isMarkedNullable) {
                return NativeKotlinType.Nullable(makeNotNullable().native)
            }

            val reflector = ObjCExportMapperReflector(namer.mapper)
            val bridge = reflector.bridgeType.invoke(this)

            return when (bridge) {
                is BlockPointerBridge -> {
                    NativeKotlinType.BlockPointer(
                        parameterTypes = arguments.dropLast(1).map { it.type.native },
                        returnType = arguments.last().type.native,
                    )
                }
                ReferenceBridge -> with(StandardNames.FqNames) {
                    when (val fqName = constructor.declarationDescriptor?.fqNameUnsafe) {
                        // Kotlin types that are not included in the Kotlin framework API
                        charSequence, number, uByteArrayFqName.toUnsafe(), uShortArrayFqName.toUnsafe(), uIntArrayFqName.toUnsafe(),
                        uLongArrayFqName.toUnsafe(),
                        -> NativeKotlinType.Any

                        string -> NativeKotlinType.Reference.Known.String
                        unit -> NativeKotlinType.Reference.Known.Unit
                        nothing -> NativeKotlinType.Reference.Known.Nothing
                        list.toUnsafe() -> NativeKotlinType.Reference.Known.List(arguments.single().type.native)
                        mutableList.toUnsafe() -> NativeKotlinType.Reference.Known.MutableList(arguments.single().type.native)
                        set.toUnsafe() -> NativeKotlinType.Reference.Known.Set(arguments.single().type.native)
                        mutableSet.toUnsafe() -> NativeKotlinType.Reference.Known.MutableSet(arguments.single().type.native)
                        map.toUnsafe() -> NativeKotlinType.Reference.Known.Map(
                            keyType = arguments.first().type.native,
                            valueType = arguments.last().type.native,
                        )
                        mutableMap.toUnsafe() -> NativeKotlinType.Reference.Known.MutableMap(
                            keyType = arguments.first().type.native,
                            valueType = arguments.last().type.native,
                        )
                        array -> NativeKotlinType.Reference.Known.Array.Generic(arguments.single().type.native)
                        else -> if (fqName != null && StandardNames.isPrimitiveArray(fqName)) {
                            NativeKotlinType.Reference.Known.Array.Primitive(arrayClassFqNameToPrimitiveType.getValue(fqName))
                        } else if (isAnyOrNullableAny()) {
                            NativeKotlinType.Any
                        } else when (val descriptor = constructor.declarationDescriptor) {
                            is ClassDescriptor -> if (isSuspendFunctionTypeOrSubtype()) {
                                NativeKotlinType.Reference.Known.SuspendFunction(
                                    this@KotlinType,
                                    descriptor,
                                    arguments.dropLast(1).map { it.type.native },
                                    arguments.last().type.native
                                )
                            } else {
                                NativeKotlinType.Reference.Unknown(this@KotlinType, descriptor)
                            }
                            is TypeParameterDescriptor -> if (descriptor.containingDeclaration is ClassDescriptor) {
                                NativeKotlinType.Reference.TypeParameter(
                                    name = namer.getTypeParameterName(descriptor)
                                )
                            } else {
                                NativeKotlinType.Any
                            }
                            else -> NativeKotlinType.Any
                        }
                    }
                }
                is ValueTypeBridge -> when (bridge.objCValueType) {
                    ObjCValueType.BOOL -> NativeKotlinType.Value.BOOL
                    ObjCValueType.UNICHAR -> NativeKotlinType.Value.UNICHAR
                    ObjCValueType.CHAR -> NativeKotlinType.Value.CHAR
                    ObjCValueType.SHORT -> NativeKotlinType.Value.SHORT
                    ObjCValueType.INT -> NativeKotlinType.Value.INT
                    ObjCValueType.LONG_LONG -> NativeKotlinType.Value.LONG_LONG
                    ObjCValueType.UNSIGNED_CHAR -> NativeKotlinType.Value.UNSIGNED_CHAR
                    ObjCValueType.UNSIGNED_SHORT -> NativeKotlinType.Value.UNSIGNED_SHORT
                    ObjCValueType.UNSIGNED_INT -> NativeKotlinType.Value.UNSIGNED_INT
                    ObjCValueType.UNSIGNED_LONG_LONG -> NativeKotlinType.Value.UNSIGNED_LONG_LONG
                    ObjCValueType.FLOAT -> NativeKotlinType.Value.FLOAT
                    ObjCValueType.DOUBLE -> NativeKotlinType.Value.DOUBLE
                    ObjCValueType.POINTER -> NativeKotlinType.Value.POINTER
                }
            }
        }

    override fun KotlinType.spec(usage: KotlinTypeSpecUsage): TypeName = native.spec(usage)

    override fun PrimitiveType.spec(usage: KotlinTypeSpecUsage): TypeName = when (this) {
        PrimitiveType.BOOLEAN -> NativeKotlinType.Value.BOOL
        PrimitiveType.CHAR -> NativeKotlinType.Value.UNICHAR
        PrimitiveType.BYTE -> NativeKotlinType.Value.CHAR
        PrimitiveType.SHORT -> NativeKotlinType.Value.SHORT
        PrimitiveType.INT -> NativeKotlinType.Value.INT
        PrimitiveType.LONG -> NativeKotlinType.Value.LONG_LONG
        PrimitiveType.FLOAT -> NativeKotlinType.Value.FLOAT
        PrimitiveType.DOUBLE -> NativeKotlinType.Value.DOUBLE
    }.spec(usage)

    override fun NativeKotlinType.spec(usage: KotlinTypeSpecUsage): TypeName {
        val path = when (usage) {
            KotlinTypeSpecUsage -> listOf(
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.ParameterType -> listOf(
                KotlinTypeSpecUsage.ParameterType,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.ParameterType.Lambda -> listOf(
                KotlinTypeSpecUsage.ParameterType.Lambda,
                KotlinTypeSpecUsage.ParameterType,
                KotlinTypeSpecUsage.TypeParam.AllowingNullability,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.ReturnType -> listOf(
                KotlinTypeSpecUsage.ReturnType,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.ReturnType.Lambda -> listOf(
                KotlinTypeSpecUsage.ReturnType.Lambda,
                KotlinTypeSpecUsage.ReturnType,
                KotlinTypeSpecUsage.TypeParam.AllowingNullability,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.ReturnType.SuspendFunction -> listOf(
                KotlinTypeSpecUsage.ReturnType.SuspendFunction,
                KotlinTypeSpecUsage.ReturnType,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam -> listOf(
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam.IsHashable -> listOf(
                KotlinTypeSpecUsage.TypeParam.IsHashable,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam.IsReference -> listOf(
                KotlinTypeSpecUsage.TypeParam.IsReference,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam.OptionalWrapped -> listOf(
                KotlinTypeSpecUsage.TypeParam.OptionalWrapped,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement -> listOf(
                KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement,
                KotlinTypeSpecUsage.TypeParam.IsReference,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )
            KotlinTypeSpecUsage.TypeParam.AllowingNullability -> listOf(
                KotlinTypeSpecUsage.TypeParam.AllowingNullability,
                KotlinTypeSpecUsage.TypeParam,
                KotlinTypeSpecUsage,
            )

        }

        path.forEach {
            exactSpec(it)?.let { return it }
        }

        error("No spec for $this with usage $usage")
    }

    fun NativeKotlinType.exactSpec(usage: KotlinTypeSpecUsage): TypeName? {
        val anyHashable = DeclaredTypeName("Swift", "AnyHashable")
        return when (this) {
            is NativeKotlinType.Nullable -> when (usage) {
                KotlinTypeSpecUsage -> type.spec(KotlinTypeSpecUsage.TypeParam.OptionalWrapped).makeOptional()
                KotlinTypeSpecUsage.ReturnType.SuspendFunction,
                KotlinTypeSpecUsage.TypeParam.AllowingNullability,
                -> type.spec(KotlinTypeSpecUsage.TypeParam.OptionalWrapped).makeOptional()
                KotlinTypeSpecUsage.TypeParam -> ANY
                KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable
                KotlinTypeSpecUsage.TypeParam.IsReference -> type.spec(KotlinTypeSpecUsage.TypeParam.IsReference)
                KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement -> ANY_OBJECT
                else -> null
            }
            is NativeKotlinType.BlockPointer -> {
                val lambdaType = FunctionTypeName.get(
                    parameters = parameterTypes.map { ParameterSpec.unnamed(it.spec(KotlinTypeSpecUsage.ParameterType.Lambda)) },
                    returnType = returnType.spec(KotlinTypeSpecUsage.ReturnType.Lambda),
                )
                when (usage) {
                    KotlinTypeSpecUsage,
                    KotlinTypeSpecUsage.TypeParam.OptionalWrapped,
                    KotlinTypeSpecUsage.TypeParam.AllowingNullability,
                    KotlinTypeSpecUsage.ReturnType,
                    -> lambdaType

                    KotlinTypeSpecUsage.ParameterType -> lambdaType.copy(attributes = listOf(AttributeSpec.ESCAPING))
                    KotlinTypeSpecUsage.ParameterType.Lambda -> lambdaType.copy(
                        attributes = listOf(AttributeSpec.ESCAPING),
                        returnType = returnType.spec(KotlinTypeSpecUsage.TypeParam.AllowingNullability),
                    )

                    KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable

                    KotlinTypeSpecUsage.TypeParam -> lambdaType.copy(
                        attributes = listOf(AttributeSpec.CONVENTION_BLOCK),
                        returnType = NativeKotlinType.Nullable(returnType).spec(KotlinTypeSpecUsage.ReturnType.Lambda),
                    )

                    KotlinTypeSpecUsage.ReturnType.Lambda -> lambdaType.copy(
                        returnType = returnType.spec(
                            if (returnType is NativeKotlinType.BlockPointer) {
                                KotlinTypeSpecUsage.ReturnType.Lambda
                            } else {
                                KotlinTypeSpecUsage.TypeParam.AllowingNullability
                            }
                        ),
                    )
                    KotlinTypeSpecUsage.ReturnType.SuspendFunction -> lambdaType.copy(
                        returnType = returnType.spec(KotlinTypeSpecUsage.TypeParam.AllowingNullability),
                    )

                    else -> null
                }
            }
            is NativeKotlinType.Reference -> when (this) {
                is NativeKotlinType.Reference.Known.Array -> when (this) {
                    is NativeKotlinType.Reference.Known.Array.Generic -> DeclaredTypeName.typeName(".KotlinArray")
                        .withTypeParameters(elementType to KotlinTypeSpecUsage.TypeParam.IsReference)
                    is NativeKotlinType.Reference.Known.Array.Primitive -> DeclaredTypeName.typeName(".Kotlin${elementType.typeName.asString()}Array")
                }
                // TODO List<List<Int>> is not translated correctly
                is NativeKotlinType.Reference.Known.List -> when (usage) {
                    KotlinTypeSpecUsage -> ARRAY.withTypeParameters(elementType to KotlinTypeSpecUsage.TypeParam)
                    KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable
                    KotlinTypeSpecUsage.TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSArray")
                    else -> null
                }
                is NativeKotlinType.Reference.Known.Set -> when (usage) {
                    KotlinTypeSpecUsage -> SET.withTypeParameters(elementType to KotlinTypeSpecUsage.TypeParam.IsHashable)

                    KotlinTypeSpecUsage.TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSSet")
                    else -> null
                }
                is NativeKotlinType.Reference.Known.Map -> when (usage) {
                    KotlinTypeSpecUsage -> DICTIONARY.withTypeParameters(
                        keyType to KotlinTypeSpecUsage.TypeParam.IsHashable,
                        valueType to KotlinTypeSpecUsage.TypeParam,
                    )

                    KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable

                    KotlinTypeSpecUsage.TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSDictionary")

                    else -> null
                }
                is NativeKotlinType.Reference.Known.MutableList -> DeclaredTypeName.typeName("Foundation.NSMutableArray")
                is NativeKotlinType.Reference.Known.MutableSet -> DeclaredTypeName.typeName(".KotlinMutableSet")
                    .withTypeParameters(elementType to KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement)
                is NativeKotlinType.Reference.Known.MutableMap -> DeclaredTypeName.typeName(".KotlinMutableDictionary")
                    .withTypeParameters(
                        keyType to KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement,
                        valueType to KotlinTypeSpecUsage.TypeParam.ObjcCollectionElement,
                    )
                NativeKotlinType.Reference.Known.String -> when (usage) {
                    KotlinTypeSpecUsage -> STRING
                    KotlinTypeSpecUsage.TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSString")
                    else -> null
                }
                NativeKotlinType.Reference.Known.Unit -> when (usage) {
                    KotlinTypeSpecUsage -> DeclaredTypeName.typeName(".KotlinUnit")
                    KotlinTypeSpecUsage.ReturnType -> VOID
                    else -> null
                }
                NativeKotlinType.Reference.Known.Nothing -> when (usage) {
                    KotlinTypeSpecUsage, KotlinTypeSpecUsage.ReturnType.SuspendFunction -> DeclaredTypeName.typeName(".KotlinNothing")
                    KotlinTypeSpecUsage.ReturnType -> VOID
                    else -> null
                }
                is NativeKotlinType.Reference.Known.SuspendFunction -> when (usage) {
                    KotlinTypeSpecUsage -> descriptor.spec

                    KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable

                    else -> null
                }
                is NativeKotlinType.Reference.TypeParameter -> TypeVariableName(name)
                is NativeKotlinType.Reference.Unknown -> {
                    if (descriptor.canBeSpecializedInSwift) {
                        descriptor.spec.withTypeParametersOf(kotlinType) { _, _ -> KotlinTypeSpecUsage.TypeParam.IsReference }
                    } else if (descriptor.kind == ClassKind.INTERFACE) {
                        when (usage) {
                            KotlinTypeSpecUsage -> descriptor.spec
                            KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable
                            else -> null
                        }
                    } else {
                        descriptor.spec
                    }
                    //     .let {
                    //     if (kotlinType.isNullable()) it.makeOptional() else it
                    // }
                }
            }
            NativeKotlinType.Value.UNICHAR -> when (usage) {
                KotlinTypeSpecUsage -> DeclaredTypeName.typeName("Foundation.unichar")
                KotlinTypeSpecUsage.TypeParam -> ANY
                KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable
                KotlinTypeSpecUsage.TypeParam.IsReference -> ANY_OBJECT
                else -> null
            }
            is NativeKotlinType.Value -> when (usage) {
                KotlinTypeSpecUsage -> when (this) {
                    NativeKotlinType.Value.BOOL -> BOOL
                    NativeKotlinType.Value.UNICHAR -> DeclaredTypeName.typeName("Foundation.unichar")
                    NativeKotlinType.Value.CHAR -> INT8
                    NativeKotlinType.Value.SHORT -> INT16
                    NativeKotlinType.Value.INT -> INT32
                    NativeKotlinType.Value.LONG_LONG -> INT64
                    NativeKotlinType.Value.UNSIGNED_CHAR -> UINT8
                    NativeKotlinType.Value.UNSIGNED_SHORT -> UIN16
                    NativeKotlinType.Value.UNSIGNED_INT -> UINT32
                    NativeKotlinType.Value.UNSIGNED_LONG_LONG -> UINT64
                    NativeKotlinType.Value.FLOAT -> FLOAT32
                    NativeKotlinType.Value.DOUBLE -> FLOAT64
                    NativeKotlinType.Value.POINTER -> TODO("Pointer")
                }

                KotlinTypeSpecUsage.TypeParam -> when (this) {
                    NativeKotlinType.Value.BOOL -> ".KotlinBoolean"
                    NativeKotlinType.Value.UNICHAR -> return ANY
                    NativeKotlinType.Value.CHAR -> ".KotlinByte"
                    NativeKotlinType.Value.SHORT -> ".KotlinShort"
                    NativeKotlinType.Value.LONG_LONG -> ".KotlinLong"
                    NativeKotlinType.Value.INT -> ".KotlinInt"
                    NativeKotlinType.Value.UNSIGNED_CHAR -> ".KotlinUByte"
                    NativeKotlinType.Value.UNSIGNED_SHORT -> ".KotlinUShort"
                    NativeKotlinType.Value.UNSIGNED_INT -> ".KotlinUInt"
                    NativeKotlinType.Value.UNSIGNED_LONG_LONG -> ".KotlinULong"
                    NativeKotlinType.Value.FLOAT -> ".KotlinFloat"
                    NativeKotlinType.Value.DOUBLE -> ".KotlinDouble"
                    NativeKotlinType.Value.POINTER -> TODO("Pointer")
                }.let(DeclaredTypeName::typeName)

                else -> null
            }
            NativeKotlinType.Any -> when (usage) {
                KotlinTypeSpecUsage -> DeclaredTypeName.typeName(".Any")

                KotlinTypeSpecUsage.TypeParam.IsHashable -> anyHashable

                KotlinTypeSpecUsage.TypeParam.IsReference -> ANY_OBJECT
                else -> null
            }
        }
    }

    private val ClassDescriptor.canBeSpecializedInSwift: Boolean
        get() = !this.kind.isInterface

    private fun DeclaredTypeName.withTypeParametersOf(
        type: KotlinType,
        usageProvider: (index: Int, argument: TypeProjection) -> KotlinTypeSpecUsage,
    ): TypeName =
        this.withTypeParameters(type.arguments.mapIndexed { index, argument -> argument.type.spec(usageProvider(index, argument)) })

    private fun DeclaredTypeName.withTypeParameters(vararg typeParameters: Pair<NativeKotlinType, KotlinTypeSpecUsage>): TypeName =
        this.withTypeParameters(typeParameters.map { (type, usage) -> type.spec(usage) })

    private fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    override val ClassDescriptor.spec: DeclaredTypeName
        get() = DeclaredTypeName.qualifiedLocalTypeName(this.swiftModel.fqName)

    override val SourceFile.spec: DeclaredTypeName
        get() = DeclaredTypeName.qualifiedLocalTypeName(this.swiftModel.fqName)

    override val PropertyDescriptor.spec: PropertySpec
        get() = PropertySpec.builder(this.swiftModel.reference, type.spec(KotlinTypeSpecUsage)).build()

    override val FunctionDescriptor.spec: FunctionSpec
        get() = TODO("Not yet implemented")
}
