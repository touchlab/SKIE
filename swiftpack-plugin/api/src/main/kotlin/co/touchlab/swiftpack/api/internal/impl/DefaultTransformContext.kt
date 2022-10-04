package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.TransformContext
import co.touchlab.swiftpack.api.internal.InternalTransformContext
import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinFile
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import co.touchlab.swiftpack.spec.symbol.KotlinSymbol

internal class DefaultTransformContext: InternalTransformContext {
    private val mutableTransforms = mutableMapOf<KotlinSymbol.Id, ApiTransformScope<*>>()
    override val transforms: List<ApiTransform>
        get() = mutableTransforms.values.map { it.build() }

    override fun KotlinClass.applyTransform(transform: TransformContext.KotlinClassTransformScope.() -> Unit): KotlinClass {
        getScope(id) {
            KotlinClassTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinProperty.applyTransform(transform: TransformContext.KotlinPropertyTransformScope.() -> Unit): KotlinProperty {
        getScope(id) {
            KotlinPropertyTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFunction.applyTransform(transform: TransformContext.KotlinFunctionTransformScope.() -> Unit): KotlinFunction {
        getScope(id) {
            KotlinFunctionTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFile.applyTransform(transform: TransformContext.KotlinFileTransformScope.() -> Unit): KotlinFile {
        getScope(id) {
            KotlinFileTransformScope(id)
        }.transform()
        return this
    }

    private fun <SCOPE: ApiTransformScope<ID>, ID: KotlinSymbol.Id> getScope(id: ID, scopeFactory: (ID) -> SCOPE): SCOPE {
        @Suppress("UNCHECKED_CAST")
        return mutableTransforms.getOrPut(id) {
            scopeFactory(id)
        } as SCOPE
    }

    private interface ApiTransformScope<ID: KotlinSymbol.Id> {
        fun build(): ApiTransform
    }

    private class KotlinClassTransformScope(
        private val classId: KotlinClass.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: ApiTransform.TypeTransform.Rename? = null,
        private var bridge: ApiTransform.TypeTransform.Bridge? = null,
    ): TransformContext.KotlinClassTransformScope, ApiTransformScope<KotlinClass.Id> {
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
        private val propertyId: KotlinProperty.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinPropertyTransformScope, ApiTransformScope<KotlinProperty.Id> {
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
        private val functionId: KotlinFunction.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinFunctionTransformScope, ApiTransformScope<KotlinFunction.Id> {
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
        private val fileId: KotlinFile.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
        private var bridge: String? = null,
    ): TransformContext.KotlinFileTransformScope, ApiTransformScope<KotlinFile.Id> {
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
