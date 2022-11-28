@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.NativeKotlinType
import co.touchlab.skie.plugin.TransformAccumulator
import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
import co.touchlab.skie.plugin.api.function.MutableSwiftFunctionName
import co.touchlab.skie.plugin.api.MutableSwiftScope
import co.touchlab.skie.plugin.api.type.MutableSwiftTypeName
import co.touchlab.skie.plugin.api.type.SwiftBridgedName
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportMapperReflector
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import io.outfoxx.swiftpoet.ANY
import io.outfoxx.swiftpoet.ARRAY
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
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType

internal class DefaultMutableSwiftScope(
    private val namer: ObjCExportNamer,
    private val transformAccumulator: TransformAccumulator,
) : MutableSwiftScope, SwiftPoetScope {
    override val ClassDescriptor.swiftName: MutableSwiftTypeName
        get() = transformAccumulator.resolveName(this)

    override var ClassDescriptor.isHiddenFromSwift: Boolean
        get() = transformAccumulator[this]?.isHidden ?: false
        set(value) {
            transformAccumulator.transform(this).isHidden = value
        }

    override var ClassDescriptor.isRemovedFromSwift: Boolean
        get() = transformAccumulator[this]?.isRemoved ?: false
        set(value) {
            transformAccumulator.transform(this).isRemoved = value
        }

    override var ClassDescriptor.swiftBridgeType: SwiftBridgedName?
        get() = transformAccumulator[this]?.bridge
        set(value) {
            transformAccumulator.transform(this).bridge = value
        }

    override val KotlinType.swiftName: String
        get() = spec(KotlinTypeSpecKind.BRIDGED).name

    override val PropertyDescriptor.originalSwiftName: String
        get() = namer.getPropertyName(this)

    override var PropertyDescriptor.swiftName: String
        get() = transformAccumulator[this]?.rename ?: originalSwiftName
        set(value) {
            transformAccumulator.transform(this).rename = value
        }

    override var PropertyDescriptor.isHiddenFromSwift: Boolean
        get() = transformAccumulator[this]?.isHidden ?: false
        set(value) {
            transformAccumulator.transform(this).isHidden = value
        }

    override var PropertyDescriptor.isRemovedFromSwift: Boolean
        get() = transformAccumulator[this]?.isRemoved ?: false
        set(value) {
            transformAccumulator.transform(this).isRemoved = value
        }

    override val FunctionDescriptor.swiftName: MutableSwiftFunctionName
        get() = transformAccumulator.resolveName(this)

    override var FunctionDescriptor.isHiddenFromSwift: Boolean
        get() = transformAccumulator[this]?.isHidden ?: false
        set(value) {
            transformAccumulator.transform(this).isHidden = value
        }

    override var FunctionDescriptor.isRemovedFromSwift: Boolean
        get() = transformAccumulator[this]?.isRemoved ?: false
        set(value) {
            transformAccumulator.transform(this).isRemoved = value
        }

    override val KotlinType.native: NativeKotlinType
        get() {
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
                        string -> NativeKotlinType.Reference.Known.String
                        unit -> NativeKotlinType.Reference.Known.Unit
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
                        } else when (val descriptor = constructor.declarationDescriptor) {
                            is ClassDescriptor -> NativeKotlinType.Reference.Unknown(this@KotlinType, descriptor)
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
                // else -> TODO("Unknown bridge type: $bridge")
            }
        }

    override fun KotlinType.spec(kind: KotlinTypeSpecKind): TypeName = native.spec(kind)

    override fun PrimitiveType.spec(kind: KotlinTypeSpecKind): TypeName = when (this) {
        PrimitiveType.BOOLEAN -> NativeKotlinType.Value.BOOL
        PrimitiveType.CHAR -> NativeKotlinType.Value.UNICHAR
        PrimitiveType.BYTE -> NativeKotlinType.Value.CHAR
        PrimitiveType.SHORT -> NativeKotlinType.Value.SHORT
        PrimitiveType.INT -> NativeKotlinType.Value.INT
        PrimitiveType.LONG -> NativeKotlinType.Value.LONG_LONG
        PrimitiveType.FLOAT -> NativeKotlinType.Value.FLOAT
        PrimitiveType.DOUBLE -> NativeKotlinType.Value.DOUBLE
    }.spec(kind)

    override fun NativeKotlinType.spec(kind: KotlinTypeSpecKind): TypeName {
        return when (this) {
            is NativeKotlinType.BlockPointer -> FunctionTypeName.get(
                parameters = parameterTypes.map { ParameterSpec.unnamed(it.spec(KotlinTypeSpecKind.SWIFT_GENERICS)) },
                returnType = returnType.spec(KotlinTypeSpecKind.SWIFT_GENERICS),
            )
            is NativeKotlinType.Reference -> when (this) {
                is NativeKotlinType.Reference.Known.Array -> when (this) {
                    is NativeKotlinType.Reference.Known.Array.Generic -> DeclaredTypeName.typeName(".KotlinArray")
                        .withTypeParameters(elementType, kind = KotlinTypeSpecKind.ORIGINAL)
                    is NativeKotlinType.Reference.Known.Array.Primitive -> DeclaredTypeName.typeName(".Kotlin${elementType.typeName.asString()}Array")
                }
                is NativeKotlinType.Reference.Known.List -> when (kind) {
                    KotlinTypeSpecKind.ORIGINAL, KotlinTypeSpecKind.SWIFT_GENERICS -> DeclaredTypeName.typeName("Foundation.NSArray")
                    KotlinTypeSpecKind.BRIDGED -> ARRAY.withTypeParameters(elementType, kind = KotlinTypeSpecKind.SWIFT_GENERICS)
                }
                is NativeKotlinType.Reference.Known.Set -> when (kind) {
                    KotlinTypeSpecKind.ORIGINAL, KotlinTypeSpecKind.SWIFT_GENERICS -> DeclaredTypeName.typeName("Foundation.NSSet")
                    KotlinTypeSpecKind.BRIDGED -> SET.withTypeParameters(elementType, kind = KotlinTypeSpecKind.SWIFT_GENERICS)
                }
                is NativeKotlinType.Reference.Known.Map -> when (kind) {
                    KotlinTypeSpecKind.ORIGINAL, KotlinTypeSpecKind.SWIFT_GENERICS -> DeclaredTypeName.typeName("Foundation.NSDictionary")
                    KotlinTypeSpecKind.BRIDGED -> DICTIONARY.withTypeParameters(
                        keyType,
                        valueType,
                        kind = KotlinTypeSpecKind.SWIFT_GENERICS
                    )
                }
                is NativeKotlinType.Reference.Known.MutableList -> DeclaredTypeName.typeName("Foundation.NSMutableArray")
                is NativeKotlinType.Reference.Known.MutableMap -> DeclaredTypeName.typeName(".KotlinMutableDictionary")
                    .withTypeParameters(keyType, valueType, kind = KotlinTypeSpecKind.ORIGINAL)
                is NativeKotlinType.Reference.Known.MutableSet -> DeclaredTypeName.typeName(".KotlinMutableSet")
                    .withTypeParameters(elementType, kind = KotlinTypeSpecKind.ORIGINAL)
                NativeKotlinType.Reference.Known.String -> when (kind) {
                    KotlinTypeSpecKind.ORIGINAL -> DeclaredTypeName.typeName("Foundation.NSString")
                    KotlinTypeSpecKind.SWIFT_GENERICS, KotlinTypeSpecKind.BRIDGED -> STRING
                }
                NativeKotlinType.Reference.Known.Unit -> VOID
                is NativeKotlinType.Reference.TypeParameter -> TypeVariableName(name)
                is NativeKotlinType.Reference.Unknown -> {
                    if (descriptor.canBeSpecializedInSwift) {
                        descriptor.spec.withTypeParameters(kotlinType, KotlinTypeSpecKind.ORIGINAL)
                    } else {
                        descriptor.spec
                    }
                }
            }
            is NativeKotlinType.Value -> when (kind) {
                KotlinTypeSpecKind.ORIGINAL, KotlinTypeSpecKind.SWIFT_GENERICS -> when (this) {
                    NativeKotlinType.Value.BOOL -> ".KotlinBoolean"
                    NativeKotlinType.Value.UNICHAR -> TODO("unichar")
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
                KotlinTypeSpecKind.BRIDGED -> when (this) {
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
            }
            NativeKotlinType.Any -> ANY
        }
    }

    private val ClassDescriptor.canBeSpecializedInSwift: Boolean
        get() = !this.kind.isInterface

    context(KotlinType)
        private fun ClassifierDescriptor?.spec(): TypeName {
        return when (this) {
            is ClassDescriptor -> spec.withTypeParameters(this@KotlinType, KotlinTypeSpecKind.ORIGINAL)
            is TypeParameterDescriptor -> if (containingDeclaration is ClassDescriptor) {
                TypeVariableName(namer.getTypeParameterName(this))
            } else {
                ANY
            }
            else -> ANY
        }
    }

    private fun DeclaredTypeName.withTypeParameters(type: KotlinType, kind: KotlinTypeSpecKind): TypeName =
        this.withTypeParameters(type.arguments.map { it.type.spec(kind) })

    private fun DeclaredTypeName.withTypeParameters(vararg typeParameters: NativeKotlinType, kind: KotlinTypeSpecKind): TypeName =
        this.withTypeParameters(typeParameters.map { it.spec(kind) })

    private fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    override val ClassDescriptor.spec: DeclaredTypeName
        get() = DeclaredTypeName.qualifiedTypeName(".${swiftName.qualifiedName}")

    override val PropertyDescriptor.spec: PropertySpec
        get() = PropertySpec.builder(swiftName, type.spec(KotlinTypeSpecKind.BRIDGED)).build()

    override val FunctionDescriptor.spec: FunctionSpec
        get() = TODO("Not yet implemented")
}
