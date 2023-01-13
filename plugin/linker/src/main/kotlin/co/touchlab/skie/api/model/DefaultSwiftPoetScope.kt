@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.property.regular.reference
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage.*
import co.touchlab.skie.plugin.api.model.type.NativeKotlinType
import co.touchlab.skie.plugin.api.model.type.bridgedOrStableSpec
import co.touchlab.skie.plugin.api.model.type.stableSpec
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportMapperReflector
import co.touchlab.skie.plugin.reflection.reflectors.mapper
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
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.builtins.isSuspendFunctionTypeOrSubtype
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.typeUtil.isAny
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable

internal class DefaultSwiftPoetScope(
    private val swiftModelScope: SwiftModelScope,
    private val namer: ObjCExportNamer,
) : SwiftPoetScope, SwiftModelScope by swiftModelScope {

    override val KotlinType.native: NativeKotlinType
        get() = with(StandardNames.FqNames) {
            when {
                isMarkedNullable -> NativeKotlinType.Nullable(makeNotNullable().native)
                isFunctionType -> NativeKotlinType.BlockPointer(
                    parameterTypes = arguments.dropLast(1).map { it.type.native },
                    returnType = arguments.last().type.native,
                )
                else -> when (val fqName = constructor.declarationDescriptor?.fqNameUnsafe) {
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
                    } else if (isAny()) {
                        NativeKotlinType.Any
                    } else {
                        val descriptor = constructor.declarationDescriptor
                        when {
                            descriptor is TypeParameterDescriptor -> if (descriptor.containingDeclaration is ClassDescriptor) {
                                NativeKotlinType.Reference.TypeParameter(
                                    name = namer.getTypeParameterName(descriptor),
                                    upperBound = descriptor.upperBounds.first().native,
                                )
                            } else {
                                descriptor.upperBounds.firstOrNull()?.native ?: NativeKotlinType.Any
                            }

                            descriptor is ClassDescriptor && isSuspendFunctionTypeOrSubtype ->
                                NativeKotlinType.Reference.Known.SuspendFunction(
                                    this@KotlinType,
                                    descriptor,
                                    arguments.dropLast(1).map { it.type.native },
                                    arguments.last().type.native
                                )

                            else -> {
                                val reflector = ObjCExportMapperReflector(namer.mapper)
                                val bridge = reflector.bridgeType.invoke(this@native)

                                when (bridge) {
                                    is BlockPointerBridge -> error("Should've been handled above!")
                                    ReferenceBridge -> NativeKotlinType.Reference.Unknown(this@KotlinType, descriptor as ClassDescriptor)
                                    is ValueTypeBridge -> when (bridge.objCValueType) {
                                        ObjCValueType.BOOL -> NativeKotlinType.Value.BOOL
                                        ObjCValueType.UNICHAR -> NativeKotlinType.Unichar
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
                                        ObjCValueType.POINTER -> if (constructor.declarationDescriptor?.fqNameUnsafe == FqNameUnsafe("kotlin.native.internal.NativePtr")) {
                                            NativeKotlinType.Pointer.NativePtr
                                        } else {
                                            NativeKotlinType.Pointer.Other
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    override fun KotlinType.spec(usage: KotlinTypeSpecUsage): TypeName = native.spec(usage)

    override fun PrimitiveType.spec(usage: KotlinTypeSpecUsage): TypeName = when (this) {
        PrimitiveType.BOOLEAN -> NativeKotlinType.Value.BOOL
        PrimitiveType.CHAR -> NativeKotlinType.Unichar
        PrimitiveType.BYTE -> NativeKotlinType.Value.CHAR
        PrimitiveType.SHORT -> NativeKotlinType.Value.SHORT
        PrimitiveType.INT -> NativeKotlinType.Value.INT
        PrimitiveType.LONG -> NativeKotlinType.Value.LONG_LONG
        PrimitiveType.FLOAT -> NativeKotlinType.Value.FLOAT
        PrimitiveType.DOUBLE -> NativeKotlinType.Value.DOUBLE
    }.spec(usage)

    override fun NativeKotlinType.spec(usage: KotlinTypeSpecUsage): TypeName {
        val path = when (usage) {
            Default -> listOf(
                Default,
            )
            ParameterType -> listOf(
                ParameterType,
                Default,
            )
            ParameterType.Lambda -> listOf(
                ParameterType.Lambda,
                ParameterType,
                TypeParam.AllowingNullability,
                TypeParam,
                Default,
            )
            ParameterType.Lambda.OptionalWrapped -> listOf(
                ParameterType.Lambda.OptionalWrapped,
                ParameterType.Lambda,
                ParameterType,
                TypeParam.AllowingNullability,
                TypeParam,
                Default,
            )
            ReturnType -> listOf(
                ReturnType,
                Default,
            )
            ReturnType.Lambda -> listOf(
                ReturnType.Lambda,
                ReturnType,
                TypeParam.AllowingNullability,
                TypeParam,
                Default,
            )
            ReturnType.Lambda.OptionalWrapped -> listOf(
                ReturnType.Lambda.OptionalWrapped,
                ReturnType.Lambda,
                ReturnType,
                TypeParam.AllowingNullability,
                TypeParam,
                Default,
            )
            ReturnType.SuspendFunction -> listOf(
                ReturnType.SuspendFunction,
                ReturnType,
                TypeParam,
                Default,
            )
            TypeParam -> listOf(
                TypeParam,
                Default,
            )
            TypeParam.IsHashable -> listOf(
                TypeParam.IsHashable,
                TypeParam,
                Default,
            )
            TypeParam.IsReference -> listOf(
                TypeParam.IsReference,
                TypeParam,
                Default,
            )
            TypeParam.OptionalWrapped -> listOf(
                TypeParam.OptionalWrapped,
                TypeParam,
                Default,
            )
            ReturnType.SuspendFunction.OptionalWrapped -> listOf(
                ReturnType.SuspendFunction.OptionalWrapped,
                TypeParam.OptionalWrapped,
                TypeParam,
                Default,
            )
            TypeParam.ObjcCollectionElement -> listOf(
                TypeParam.ObjcCollectionElement,
                TypeParam.IsReference,
                TypeParam,
                Default,
            )
            TypeParam.AllowingNullability -> listOf(
                TypeParam.AllowingNullability,
                TypeParam,
                Default,
            )

        }

        return path.firstNotNullOfOrNull { exactSpec(it) } ?: error("No spec for $this with usage $usage")
    }

    private fun NativeKotlinType.exactSpec(usage: KotlinTypeSpecUsage): TypeName? {
        val any = DeclaredTypeName.typeName(".Any")
        val anyHashable = DeclaredTypeName("Swift", "AnyHashable")
        fun DefaultOnly(name: TypeName): TypeName? = if (usage == Default) {
            name
        } else {
            null
        }

        fun Always(name: TypeName): TypeName = name

        return when (this) {
            is NativeKotlinType.Nullable -> when (usage) {
                Default -> type.spec(TypeParam.OptionalWrapped).makeOptional()
                ReturnType.SuspendFunction -> type.spec(ReturnType.SuspendFunction.OptionalWrapped).makeOptional()
                TypeParam.AllowingNullability -> type.spec(TypeParam.OptionalWrapped).makeOptional()
                TypeParam -> any
                TypeParam.IsHashable -> anyHashable
                TypeParam.IsReference -> type.spec(TypeParam.IsReference)
                TypeParam.ObjcCollectionElement -> ANY_OBJECT
                ParameterType.Lambda -> type.spec(ParameterType.Lambda.OptionalWrapped).makeOptional()
                ReturnType.Lambda -> type.spec(ReturnType.Lambda.OptionalWrapped).makeOptional()
                else -> null
            }
            is NativeKotlinType.BlockPointer -> {
                val lambdaType = FunctionTypeName.get(
                    parameters = parameterTypes.map { ParameterSpec.unnamed(it.spec(ParameterType.Lambda)) },
                    returnType = returnType.spec(ReturnType.Lambda),
                )
                when (usage) {
                    Default, TypeParam.OptionalWrapped, TypeParam.AllowingNullability, ReturnType -> lambdaType
                    ParameterType -> lambdaType.copy(attributes = listOf(AttributeSpec.ESCAPING))
                    ParameterType.Lambda -> lambdaType.copy(
                        attributes = listOf(AttributeSpec.ESCAPING),
                        returnType = returnType.spec(TypeParam.AllowingNullability),
                    )
                    ParameterType.Lambda.OptionalWrapped -> lambdaType.copy(
                        attributes = emptyList(),
                        returnType = returnType.spec(TypeParam.AllowingNullability),
                    )

                    TypeParam.IsHashable -> anyHashable

                    TypeParam -> lambdaType.copy(
                        attributes = listOf(AttributeSpec.CONVENTION_BLOCK),
                        returnType = NativeKotlinType.Nullable(returnType).spec(ReturnType.Lambda),
                    )

                    ReturnType.Lambda -> lambdaType.copy(
                        returnType = returnType.spec(
                            if (returnType is NativeKotlinType.BlockPointer) {
                                ReturnType.Lambda
                            } else {
                                TypeParam.AllowingNullability
                            }
                        ),
                    )
                    ReturnType.SuspendFunction -> lambdaType.copy(
                        returnType = returnType.spec(
                            if (returnType == NativeKotlinType.Pointer.NativePtr || (returnType as? NativeKotlinType.Reference.TypeParameter)?.upperBound == NativeKotlinType.Pointer.NativePtr) {
                                ReturnType.SuspendFunction
                            } else {
                                TypeParam.AllowingNullability
                            }
                        ),
                    )

                    else -> null
                }
            }
            is NativeKotlinType.Reference -> when (this) {
                is NativeKotlinType.Reference.Known.Array -> when (this) {
                    is NativeKotlinType.Reference.Known.Array.Generic -> DefaultOnly(
                        DeclaredTypeName.typeName(".KotlinArray").withTypeParameters(elementType to TypeParam.IsReference)
                    )
                    is NativeKotlinType.Reference.Known.Array.Primitive -> DefaultOnly(
                        DeclaredTypeName.typeName(".Kotlin${elementType.typeName.asString()}Array")
                    )
                }
                is NativeKotlinType.Reference.Known.List -> when (usage) {
                    Default -> ARRAY.withTypeParameters(elementType to TypeParam)
                    TypeParam.IsHashable -> anyHashable
                    TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSArray")
                    else -> null
                }
                is NativeKotlinType.Reference.Known.Set -> when (usage) {
                    Default -> SET.withTypeParameters(elementType to TypeParam.IsHashable)

                    TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSSet")
                    else -> null
                }
                is NativeKotlinType.Reference.Known.Map -> when (usage) {
                    Default -> DICTIONARY.withTypeParameters(
                        keyType to TypeParam.IsHashable,
                        valueType to TypeParam,
                    )

                    TypeParam.IsHashable -> anyHashable

                    TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSDictionary")

                    else -> null
                }
                is NativeKotlinType.Reference.Known.MutableList -> Always(
                    DeclaredTypeName.typeName("Foundation.NSMutableArray")
                )
                is NativeKotlinType.Reference.Known.MutableSet -> Always(
                    DeclaredTypeName.typeName(".KotlinMutableSet")
                        .withTypeParameters(elementType to TypeParam.ObjcCollectionElement)
                )
                is NativeKotlinType.Reference.Known.MutableMap -> Always(
                    DeclaredTypeName.typeName(".KotlinMutableDictionary")
                        .withTypeParameters(
                            keyType to TypeParam.ObjcCollectionElement,
                            valueType to TypeParam.ObjcCollectionElement,
                        )
                )
                NativeKotlinType.Reference.Known.String -> when (usage) {
                    Default -> STRING
                    TypeParam.IsReference -> DeclaredTypeName.typeName("Foundation.NSString")
                    else -> null
                }
                NativeKotlinType.Reference.Known.Unit -> when (usage) {
                    Default, ReturnType.Lambda.OptionalWrapped -> DeclaredTypeName.typeName(".KotlinUnit")
                    ReturnType -> VOID
                    else -> null
                }
                NativeKotlinType.Reference.Known.Nothing -> when (usage) {
                    Default, ReturnType.SuspendFunction, ReturnType.Lambda.OptionalWrapped -> DeclaredTypeName.typeName(".KotlinNothing")
                    ReturnType -> VOID
                    else -> null
                }
                is NativeKotlinType.Reference.Known.SuspendFunction -> when (usage) {
                    Default -> descriptor.swiftModel.bridgedOrStableSpec

                    TypeParam.IsHashable -> anyHashable

                    else -> null
                }
                is NativeKotlinType.Reference.TypeParameter -> {
                    when (val bound = upperBound) {
                        is NativeKotlinType.Nullable -> {
                            val unwrappedTypeParam = NativeKotlinType.Reference.TypeParameter(name, bound.type)
                            when (usage) {
                                Default -> unwrappedTypeParam.spec(TypeParam.OptionalWrapped).makeOptional()
                                TypeParam.AllowingNullability -> if (bound.type is NativeKotlinType.Pointer) {
                                    TypeVariableName(name).makeOptional()
                                } else {
                                    unwrappedTypeParam.spec(TypeParam.OptionalWrapped).makeOptional()
                                }
                                ReturnType.SuspendFunction, ReturnType.SuspendFunction.OptionalWrapped -> unwrappedTypeParam.spec(ReturnType.SuspendFunction.OptionalWrapped)
                                    .makeOptional()
                                TypeParam -> any
                                TypeParam.IsReference -> unwrappedTypeParam.spec(TypeParam.IsReference)
                                TypeParam.IsHashable -> anyHashable
                                TypeParam.OptionalWrapped -> unwrappedTypeParam.spec(TypeParam.OptionalWrapped)
                                TypeParam.ObjcCollectionElement -> ANY_OBJECT
                                ParameterType.Lambda -> unwrappedTypeParam.spec(ParameterType.Lambda.OptionalWrapped).makeOptional()
                                ReturnType.Lambda -> unwrappedTypeParam.spec(ReturnType.Lambda.OptionalWrapped).makeOptional()
                                else -> null
                            }
                        }

                        is NativeKotlinType.BlockPointer -> bound.exactSpec(usage)

                        is NativeKotlinType.Value,
                        is NativeKotlinType.Reference.Known.String,
                        -> bound.exactSpec(usage)

                        is NativeKotlinType.Reference.Known.List,
                        is NativeKotlinType.Reference.Known.MutableList,
                        is NativeKotlinType.Reference.Known.Map,
                        is NativeKotlinType.Reference.Known.MutableMap,
                        is NativeKotlinType.Reference.Known.Set,
                        is NativeKotlinType.Reference.Known.MutableSet,
                        -> bound.exactSpec(usage)

                        is NativeKotlinType.Unichar -> when (usage) {
                            Default -> bound.exactSpec(Default)
                            ParameterType.Lambda, ReturnType.Lambda, ReturnType.SuspendFunction, TypeParam -> TypeVariableName(name)
                            TypeParam.IsHashable -> anyHashable
                            else -> null
                        }
                        NativeKotlinType.Pointer.Other -> when (usage) {
                            Default -> bound.exactSpec(Default)
                            ParameterType.Lambda,
                            ParameterType.Lambda.OptionalWrapped,
                            ReturnType.Lambda,
                            ReturnType.Lambda.OptionalWrapped,
                            ReturnType.SuspendFunction,
                            ReturnType.SuspendFunction.OptionalWrapped,
                            TypeParam,
                            -> TypeVariableName(name)
                            TypeParam.IsHashable -> anyHashable
                            TypeParam.OptionalWrapped -> bound.exactSpec(TypeParam.OptionalWrapped)
                            else -> null
                        }
                        NativeKotlinType.Pointer.NativePtr -> when (usage) {
                            Default -> bound.exactSpec(Default)
                            ParameterType.Lambda, ReturnType.Lambda, ReturnType.SuspendFunction, TypeParam.AllowingNullability -> TypeVariableName(
                                name
                            ).makeOptional()
                            TypeParam, ReturnType.SuspendFunction.OptionalWrapped -> TypeVariableName(name)
                            TypeParam.IsHashable -> anyHashable
                            else -> null
                        }

                        else -> when (usage) {
                            Default -> TypeVariableName(name)
                            TypeParam.IsHashable -> anyHashable
                            else -> null
                        }
                    }
                }

                is NativeKotlinType.Reference.Unknown -> {
                    if (descriptor.canBeSpecializedInSwift) {
                        when (usage) {
                            Default -> descriptor.swiftModel.bridgedOrStableSpec.withTypeParametersOf(kotlinType) { _, _ ->
                                if (descriptor.swiftModel.isSwiftSymbol) {
                                    TypeParam.IsReference
                                } else {
                                    TypeParam
                                }
                            }
                            TypeParam.IsReference -> descriptor.swiftModel.stableSpec.withTypeParametersOf(kotlinType) { _, _ ->
                                TypeParam.IsReference
                            }
                            else -> null
                        }
                    } else if (descriptor.kind == ClassKind.INTERFACE) {
                        when (usage) {
                            Default -> descriptor.swiftModel.bridgedOrStableSpec
                            TypeParam.IsHashable -> anyHashable
                            else -> null
                        }
                    } else {
                        DefaultOnly(descriptor.swiftModel.bridgedOrStableSpec)
                    }
                }
            }
            NativeKotlinType.Unichar -> when (usage) {
                Default -> DeclaredTypeName.typeName("Foundation.unichar")
                TypeParam -> any
                TypeParam.IsHashable -> anyHashable
                TypeParam.IsReference -> ANY_OBJECT
                else -> null
            }
            NativeKotlinType.Pointer.NativePtr -> when (usage) {
                Default -> DeclaredTypeName.typeName("Swift.UnsafeMutableRawPointer").makeOptional()
                TypeParam, ReturnType.SuspendFunction.OptionalWrapped -> any
                TypeParam.IsHashable -> anyHashable
                TypeParam.IsReference -> ANY_OBJECT
                ParameterType.Lambda, ReturnType.Lambda -> any.makeOptional()
                ReturnType.SuspendFunction -> any.makeOptional()
                else -> null
            }
            NativeKotlinType.Pointer.Other -> when (usage) {
                Default, TypeParam.OptionalWrapped -> DeclaredTypeName.typeName("Swift.UnsafeMutableRawPointer")
                TypeParam,
                ParameterType.Lambda.OptionalWrapped,
                ReturnType.Lambda.OptionalWrapped,
                ReturnType.SuspendFunction.OptionalWrapped,
                -> any
                TypeParam.IsHashable -> anyHashable
                TypeParam.IsReference -> ANY_OBJECT
                else -> null
            }
            is NativeKotlinType.Value -> when (usage) {
                Default -> @Suppress("KotlinConstantConditions") when (this) {
                    NativeKotlinType.Value.BOOL -> BOOL
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
                }

                TypeParam -> @Suppress("KotlinConstantConditions") when (this) {
                    NativeKotlinType.Value.BOOL -> ".KotlinBoolean"
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
                }.let(DeclaredTypeName::typeName)

                else -> null
            }
            NativeKotlinType.Any -> when (usage) {
                Default -> any

                TypeParam.IsHashable -> anyHashable

                TypeParam.IsReference -> ANY_OBJECT
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

    override val PropertyDescriptor.regularPropertySpec: PropertySpec
        get() = PropertySpec.builder(this.regularPropertySwiftModel.reference, type.spec(Default)).build()

    override val PropertyDescriptor.interfaceExtensionPropertySpec: FunctionSpec
        get() = TODO("Not yet implemented")

    override val FunctionDescriptor.spec: FunctionSpec
        get() = TODO("Not yet implemented")
}
