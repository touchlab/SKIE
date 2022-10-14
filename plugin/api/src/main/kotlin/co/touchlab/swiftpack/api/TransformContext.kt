package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinFileReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference

interface TransformContext {
    fun KotlinClassReference.applyTransform(transform: KotlinClassTransformScope.() -> Unit): KotlinClassReference

    fun KotlinPropertyReference.applyTransform(transform: KotlinPropertyTransformScope.() -> Unit): KotlinPropertyReference

    fun KotlinFunctionReference.applyTransform(transform: KotlinFunctionTransformScope.() -> Unit): KotlinFunctionReference

    fun KotlinFileReference.applyTransform(transform: KotlinFileTransformScope.() -> Unit): KotlinFileReference

    @DslMarker
    annotation class TransformScopeMarker

    @TransformScopeMarker
    interface KotlinClassTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)

        fun rename(newSwiftName: ApiTransform.TypeTransform.Rename)

        fun bridge(swiftType: String)

        fun bridge(bridge: ApiTransform.TypeTransform.Bridge)
    }

    @TransformScopeMarker
    interface KotlinPropertyTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)
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
