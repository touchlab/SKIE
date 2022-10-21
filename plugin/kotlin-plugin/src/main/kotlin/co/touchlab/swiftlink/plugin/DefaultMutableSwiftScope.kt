@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.reflection.reflectors.ObjCExportMapperReflector
import co.touchlab.swiftlink.plugin.reflection.reflectors.mapper
import co.touchlab.swiftpack.api.MutableSwiftFunctionName
import co.touchlab.swiftpack.api.MutableSwiftScope
import co.touchlab.swiftpack.api.MutableSwiftTypeName
import co.touchlab.swiftpack.api.SwiftBridgedName
import co.touchlab.swiftpack.api.SwiftFunctionName
import co.touchlab.swiftpack.api.SwiftPoetScope
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FLOAT32
import io.outfoxx.swiftpoet.FLOAT64
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT16
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64
import io.outfoxx.swiftpoet.INT8
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.UIN16
import io.outfoxx.swiftpoet.UINT32
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.UINT8
import io.outfoxx.swiftpoet.VOID
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCValueType
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnit

internal class DefaultMutableSwiftScope(
    private val namer: ObjCExportNamer,
    private val transformAccumulator: TransformAccumulator,
    private val moduleName: String,
): MutableSwiftScope, SwiftPoetScope {
    override var ClassDescriptor.swiftName: MutableSwiftTypeName
        get() = transformAccumulator.resolveName(TransformAccumulator.TypeTransformTarget.Class(this))
        set(value) {
            transformAccumulator.transform(this).swiftName = value
        }

    override var ClassDescriptor.isHiddenFromSwift: Boolean
        get() = transformAccumulator[this]?.isHidden ?: false
        set(value) {
            transformAccumulator.transform(this).isHidden = value
            if (value && !swiftName.simpleName.startsWith("__")) {
                swiftName.simpleName = "__${swiftName.simpleName}"
            }
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
        get() = spec.name

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

    override var FunctionDescriptor.swiftName: MutableSwiftFunctionName
        get() = transformAccumulator.resolveName(this)
        set(value) {
            transformAccumulator.transform(this).swiftName = value
        }

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

    override val KotlinType.spec: TypeName
        get() {
            val reflector = ObjCExportMapperReflector(namer.mapper)
            // val bridge = namer.mapper.bridgeType(this)
            val bridge = reflector.bridgeType.invoke(this)

            return when (bridge) {
                is BlockPointerBridge -> TODO()
                ReferenceBridge -> when {
                    KotlinBuiltIns.isString(this) -> STRING
                    isUnit() -> VOID
                    else -> (constructor.declarationDescriptor as ClassDescriptor).spec
                }
                is ValueTypeBridge -> when (bridge.objCValueType) {
                    ObjCValueType.BOOL -> BOOL
                    ObjCValueType.UNICHAR -> TODO()
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
                    ObjCValueType.POINTER -> TODO()
                }
                else -> TODO()
            }
        }

    override val ClassDescriptor.spec: DeclaredTypeName
        get() = DeclaredTypeName.qualifiedTypeName("$moduleName.${swiftName.qualifiedName}")

    override val PropertyDescriptor.spec: PropertySpec
        get() = TODO("Not yet implemented")

    override val FunctionDescriptor.spec: FunctionSpec
        get() = TODO("Not yet implemented")
}
