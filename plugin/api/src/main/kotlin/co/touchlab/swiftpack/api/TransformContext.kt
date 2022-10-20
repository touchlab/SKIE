package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinFileReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

interface TransformContext {

    fun ClassDescriptor.applyTransform(transform: KotlinClassTransformScope.() -> Unit)
    fun IrClass.applyTransform(transform: KotlinClassTransformScope.() -> Unit)

    fun PropertyDescriptor.applyTransform(transform: KotlinPropertyTransformScope.() -> Unit)
    fun IrProperty.applyTransform(transform: KotlinPropertyTransformScope.() -> Unit)

    fun FunctionDescriptor.applyTransform(transform: KotlinFunctionTransformScope.() -> Unit)
    fun IrFunction.applyTransform(transform: KotlinFunctionTransformScope.() -> Unit)

    @DslMarker
    annotation class TransformScopeMarker

    @TransformScopeMarker
    interface KotlinClassTransformScope {
        fun remove()

        fun hide()

        fun rename(newName: String)

        fun bridge(swiftType: String)
    }

    @TransformScopeMarker
    interface KotlinPropertyTransformScope {
        fun remove()

        fun hide()

        fun rename(newName: String)
    }

    @TransformScopeMarker
    interface KotlinFunctionTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)
    }

    @TransformScopeMarker
    interface KotlinFileTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)

        fun bridge(swiftType: String)
    }
}
