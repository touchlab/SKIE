package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.isPublicApi
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.render

@Suppress("ktlint:standard:function-naming")
actual fun IrTypeParameterPublicSymbolImpl(signature: IdSignature, descriptor: TypeParameterDescriptor): IrTypeParameterSymbol =
    IrTypeParameterSymbolImpl(descriptor, signature)

actual abstract class IrBaseRebindablePublicSymbol<out Descriptor : DeclarationDescriptor, Owner : IrSymbolOwner> actual constructor(
    actual override val signature: IdSignature,
    descriptor: Descriptor,
) : IrBindableSymbol<Descriptor, Owner> {

//     private var _owner: B? = null
//     override val owner: B
//         get() = _owner ?: throw IllegalStateException("Symbol is not bound")
//
//     override fun bind(owner: B) {
//         this._owner = owner
//     }
//
//     fun unbind() {
//         this._owner = null
//     }
//
//     override val isBound: Boolean
//         get() = _owner != null
//
//     override var privateSignature: IdSignature? = null

    private val _descriptor: Descriptor? = descriptor

    @ObsoleteDescriptorBasedAPI
    @Suppress("UNCHECKED_CAST")
    actual override val descriptor: Descriptor
        get() = _descriptor ?: (owner as IrDeclaration).toIrBasedDescriptor() as Descriptor

    @ObsoleteDescriptorBasedAPI
    actual override val hasDescriptor: Boolean
        get() = _descriptor != null

    private var _owner: Owner? = null
    actual override val owner: Owner
        get() = _owner ?: error("${javaClass.simpleName} is unbound. Signature: $signature")

    actual override var privateSignature: IdSignature? = null

    init {
        assert(descriptor == null || isOriginalDescriptor(descriptor)) {
            "Substituted descriptor $descriptor for ${descriptor!!.original}"
        }
        if (!isPublicApi && descriptor != null) {
            val containingDeclaration = descriptor.containingDeclaration
            assert(containingDeclaration == null || isOriginalDescriptor(containingDeclaration)) {
                "Substituted containing declaration: $containingDeclaration\nfor descriptor: $descriptor"
            }
        }
    }

    private fun isOriginalDescriptor(descriptor: DeclarationDescriptor): Boolean =
        // TODO fix declaring/referencing value parameters: compute proper original descriptor
        descriptor is ValueParameterDescriptor &&
            isOriginalDescriptor(descriptor.containingDeclaration) ||
            descriptor == descriptor.original

    actual override val isBound: Boolean
        get() = _owner != null

    actual override fun bind(owner: Owner) {
        this._owner = owner
    }

    actual fun unbind() {
        this._owner = null
    }

    override fun toString(): String {
        if (isBound) return owner.render()
        return if (isPublicApi) {
            "Unbound public symbol ${this::class.java.simpleName}: $signature"
        } else {
            "Unbound private symbol " +
                if (_descriptor != null) "${this::class.java.simpleName}: $_descriptor" else super.toString()
        }
    }
}
