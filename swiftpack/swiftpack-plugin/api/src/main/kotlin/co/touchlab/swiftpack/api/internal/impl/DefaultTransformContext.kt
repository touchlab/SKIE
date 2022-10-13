package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.TransformContext
import co.touchlab.swiftpack.api.internal.InternalTransformContext
import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinFileReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference

internal class DefaultTransformContext: InternalTransformContext {
    private val mutableTransforms = mutableMapOf<KotlinDeclarationReference.Id, ApiTransformScope<*>>()
    override val transforms: List<ApiTransform>
        get() = mutableTransforms.values.map { it.build() }

    override fun KotlinClassReference.applyTransform(transform: TransformContext.KotlinClassTransformScope.() -> Unit): KotlinClassReference {
        getScope(id) {
            KotlinClassTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinPropertyReference.applyTransform(transform: TransformContext.KotlinPropertyTransformScope.() -> Unit): KotlinPropertyReference {
        getScope(id) {
            KotlinPropertyTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFunctionReference.applyTransform(transform: TransformContext.KotlinFunctionTransformScope.() -> Unit): KotlinFunctionReference {
        getScope(id) {
            KotlinFunctionTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFileReference.applyTransform(transform: TransformContext.KotlinFileTransformScope.() -> Unit): KotlinFileReference {
        getScope(id) {
            KotlinFileTransformScope(id)
        }.transform()
        return this
    }

    private fun <SCOPE: ApiTransformScope<ID>, ID: KotlinDeclarationReference.Id> getScope(id: ID, scopeFactory: (ID) -> SCOPE): SCOPE {
        @Suppress("UNCHECKED_CAST")
        return mutableTransforms.getOrPut(id) {
            scopeFactory(id)
        } as SCOPE
    }

    private interface ApiTransformScope<ID: KotlinDeclarationReference.Id> {
        fun build(): ApiTransform
    }

    private class KotlinClassTransformScope(
        private val classId: KotlinClassReference.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: ApiTransform.TypeTransform.Rename? = null,
        private var bridge: ApiTransform.TypeTransform.Bridge? = null,
    ): TransformContext.KotlinClassTransformScope, ApiTransformScope<KotlinClassReference.Id> {
         override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename(
                ApiTransform.TypeTransform.Rename(
                    kind = ApiTransform.TypeTransform.Rename.Kind.ABSOLUTE,
                    action = ApiTransform.TypeTransform.Rename.Action.Replace(newSwiftName),
                )
            )
        }

        override fun rename(rename: ApiTransform.TypeTransform.Rename) {
            this.rename = rename
        }

        override fun bridge(swiftType: String) {
            bridge(ApiTransform.TypeTransform.Bridge.Absolute(swiftType))
        }

        override fun bridge(bridge: ApiTransform.TypeTransform.Bridge) {
            this.bridge = bridge
        }

        override fun build(): ApiTransform = ApiTransform.TypeTransform(
            typeId = classId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
            bridge = bridge,
        )
    }

    private class KotlinPropertyTransformScope(
        private val propertyId: KotlinPropertyReference.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinPropertyTransformScope, ApiTransformScope<KotlinPropertyReference.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun build(): ApiTransform = ApiTransform.PropertyTransform(
            propertyId = propertyId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
        )
    }

    private class KotlinFunctionTransformScope(
        private val functionId: KotlinFunctionReference.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinFunctionTransformScope, ApiTransformScope<KotlinFunctionReference.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun build(): ApiTransform = ApiTransform.FunctionTransform(
            functionId = functionId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
        )
    }

    private class KotlinFileTransformScope(
        private val fileId: KotlinFileReference.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
        private var bridge: String? = null,
    ): TransformContext.KotlinFileTransformScope, ApiTransformScope<KotlinFileReference.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun bridge(swiftType: String) {
            bridge = swiftType
        }

        override fun build(): ApiTransform = ApiTransform.FileTransform(
            fileId = fileId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename?.let { ApiTransform.TypeTransform.Rename.Action.Replace(it) },
            bridge = bridge,
        )
    }
}
