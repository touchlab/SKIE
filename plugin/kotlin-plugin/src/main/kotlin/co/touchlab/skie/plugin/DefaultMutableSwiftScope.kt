@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.api.KotlinTypeSpecKind
import co.touchlab.skie.plugin.api.MutableSwiftFunctionName
import co.touchlab.skie.plugin.api.MutableSwiftScope
import co.touchlab.skie.plugin.api.MutableSwiftTypeName
import co.touchlab.skie.plugin.api.SwiftBridgedName
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
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.types.KotlinType

internal class DefaultMutableSwiftScope(
    private val namer: ObjCExportNamer,
    private val transformAccumulator: TransformAccumulator,
    private val moduleName: String,
) : MutableSwiftScope, SwiftPoetScope {
    override val ClassDescriptor.swiftName: MutableSwiftTypeName
        get() = transformAccumulator.resolveName(TransformAccumulator.TypeTransformTarget.Class(this))

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

    override fun KotlinType.spec(kind: KotlinTypeSpecKind): TypeName {
        val reflector = ObjCExportMapperReflector(namer.mapper)
        val bridge = reflector.bridgeType.invoke(this)

        return when (bridge) {
            is BlockPointerBridge -> {
                FunctionTypeName.get(
                    parameters = arguments.dropLast(1).map { ParameterSpec.unnamed(it.type.spec(KotlinTypeSpecKind.SWIFT_GENERICS)) },
                    returnType = arguments.last().type.spec(KotlinTypeSpecKind.SWIFT_GENERICS),
                )
            }
            ReferenceBridge -> with(StandardNames.FqNames) {
                when (kind) {
                    KotlinTypeSpecKind.ORIGINAL -> when (constructor.declarationDescriptor?.fqNameUnsafe) {
                        string -> DeclaredTypeName.typeName("Foundation.NSString")
                        unit -> VOID
                        else -> (constructor.declarationDescriptor as ClassDescriptor).spec.withTypeParameters(this@spec, KotlinTypeSpecKind.ORIGINAL)
                    }
                    KotlinTypeSpecKind.SWIFT_GENERICS -> when (constructor.declarationDescriptor?.fqNameUnsafe) {
                        string -> STRING
                        unit -> VOID
                        else -> (constructor.declarationDescriptor as ClassDescriptor).spec.withTypeParameters(this@spec, KotlinTypeSpecKind.ORIGINAL)
                    }
                    KotlinTypeSpecKind.BRIDGED -> when (constructor.declarationDescriptor?.fqNameUnsafe) {
                        string -> STRING
                        unit -> VOID
                        list.toUnsafe() -> ARRAY.withTypeParameters(this@spec, KotlinTypeSpecKind.SWIFT_GENERICS)
                        set.toUnsafe() -> SET.withTypeParameters(this@spec, KotlinTypeSpecKind.SWIFT_GENERICS)
                        map.toUnsafe() -> DICTIONARY.withTypeParameters(this@spec, KotlinTypeSpecKind.SWIFT_GENERICS)
                        mutableList.toUnsafe() -> DeclaredTypeName.typeName("Foundation.NSMutableArray")
                        else -> constructor.declarationDescriptor.spec()
                    }
                }
            }
            is ValueTypeBridge -> when (kind) {
                KotlinTypeSpecKind.ORIGINAL, KotlinTypeSpecKind.SWIFT_GENERICS -> when (bridge.objCValueType) {
                    ObjCValueType.BOOL -> ".KotlinBoolean"
                    ObjCValueType.UNICHAR -> TODO()
                    ObjCValueType.CHAR -> ".KotlinByte"
                    ObjCValueType.SHORT -> ".KotlinShort"
                    ObjCValueType.LONG_LONG -> ".KotlinLong"
                    ObjCValueType.INT -> ".KotlinInt"
                    ObjCValueType.UNSIGNED_CHAR -> ".KotlinUByte"
                    ObjCValueType.UNSIGNED_SHORT -> ".KotlinUShort"
                    ObjCValueType.UNSIGNED_INT -> ".KotlinUInt"
                    ObjCValueType.UNSIGNED_LONG_LONG -> ".KotlinULong"
                    ObjCValueType.FLOAT -> ".KotlinFloat"
                    ObjCValueType.DOUBLE -> ".KotlinDouble"
                    ObjCValueType.POINTER -> TODO()
                }.let(DeclaredTypeName::typeName)
                KotlinTypeSpecKind.BRIDGED -> when (bridge.objCValueType) {
                    ObjCValueType.BOOL -> BOOL
                    ObjCValueType.UNICHAR -> TODO("unichar ${bridge.objCValueType}")
                    ObjCValueType.CHAR -> INT8
                    ObjCValueType.SHORT -> INT16
                    ObjCValueType.INT -> INT32
                    ObjCValueType.LONG_LONG -> INT64
                    ObjCValueType.UNSIGNED_CHAR -> UINT8
                    ObjCValueType.UNSIGNED_SHORT -> UIN16
                    ObjCValueType.UNSIGNED_INT -> UINT32
                    ObjCValueType.UNSIGNED_LONG_LONG -> UINT64
                    ObjCValueType.FLOAT -> FLOAT32
                    ObjCValueType.DOUBLE -> FLOAT64
                    ObjCValueType.POINTER -> TODO("Pointer ${bridge.objCValueType}")
                }
            }
            else -> TODO("Unknown bridge type: $bridge")
        }
    }

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

    private fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    override val ClassDescriptor.spec: DeclaredTypeName
        get() = DeclaredTypeName.qualifiedTypeName("$moduleName.${swiftName.qualifiedName}")

    override val PropertyDescriptor.spec: PropertySpec
        get() = TODO("Not yet implemented")

    override val FunctionDescriptor.spec: FunctionSpec
        get() = TODO("Not yet implemented")
}
