package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.TransformContext
import co.touchlab.swiftpack.api.internal.InternalTransformContext
import co.touchlab.swiftpack.spec.module.ApiTransform
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinFileReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

@OptIn(ObsoleteDescriptorBasedAPI::class)
internal class DefaultTransformContext: InternalTransformContext {
    private val typeTransforms = mutableMapOf<DeclarationDescriptor, ObjcClassTransformScope>()

    override fun ClassDescriptor.applyTransform(transform: TransformContext.KotlinClassTransformScope.() -> Unit) {
        typeTransforms.getOrPut(this) { ObjcClassTransformScope() }.transform()
    }

    override fun IrClass.applyTransform(transform: TransformContext.KotlinClassTransformScope.() -> Unit) {
        symbol.descriptor.applyTransform(transform)
    }

    override fun PropertyDescriptor.applyTransform(transform: TransformContext.KotlinPropertyTransformScope.() -> Unit) {
        typeTransform(containingDeclaration).properties.getOrPut(this) { ObjcPropertyTransformScope() }.transform()
    }

    override fun IrProperty.applyTransform(transform: TransformContext.KotlinPropertyTransformScope.() -> Unit) {
        symbol.descriptor.applyTransform(transform)
    }

    override fun FunctionDescriptor.applyTransform(transform: TransformContext.KotlinFunctionTransformScope.() -> Unit) {
        typeTransform(containingDeclaration).methods.getOrPut(this) { ObjcMethodTransformScope() }.transform()
    }

    override fun IrFunction.applyTransform(transform: TransformContext.KotlinFunctionTransformScope.() -> Unit) {
        symbol.descriptor.applyTransform(transform)
    }

    private fun typeTransform(declaration: DeclarationDescriptor): ObjcClassTransformScope {
        return typeTransforms.getOrPut(declaration) {
            ObjcClassTransformScope()
        }
    }

    private class ObjcClassTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var newSwiftName: String? = null,
        var bridge: String? = null,
    ): TransformContext.KotlinClassTransformScope {
        val properties = mutableMapOf<PropertyDescriptor, ObjcPropertyTransformScope>()
        val methods = mutableMapOf<FunctionDescriptor, ObjcMethodTransformScope>()

        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newName: String) {
            newSwiftName = newName
        }

        override fun bridge(swiftType: String) {
            bridge = swiftType
        }
    }

    private class ObjcPropertyTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var rename: String? = null,
    ): TransformContext.KotlinPropertyTransformScope {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }
    }

    private class ObjcMethodTransformScope(
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinFunctionTransformScope {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }
    }
}
