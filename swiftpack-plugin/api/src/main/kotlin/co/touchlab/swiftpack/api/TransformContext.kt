package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinFile
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty

interface TransformContext {
    fun KotlinClass.applyTransform(transform: KotlinClassTransformScope.() -> Unit): KotlinClass

    fun KotlinProperty.applyTransform(transform: KotlinPropertyTransformScope.() -> Unit): KotlinProperty

    fun KotlinFunction.applyTransform(transform: KotlinFunctionTransformScope.() -> Unit): KotlinFunction

    fun KotlinFile.applyTransform(transform: KotlinFileTransformScope.() -> Unit): KotlinFile

    @DslMarker
    annotation class TransformScopeMarker

    @TransformScopeMarker
    interface KotlinClassTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)

        fun rename(newSwiftName: ApiTransform.TypeTransform.Rename)

        fun bridge(bridge: ApiTransform.TypeTransform.Bridge)

        fun bridge(swiftType: String)
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
