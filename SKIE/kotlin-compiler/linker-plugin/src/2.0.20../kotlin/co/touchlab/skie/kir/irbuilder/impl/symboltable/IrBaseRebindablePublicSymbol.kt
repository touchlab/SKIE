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

fun getIrTypeParameterPublicSymbolImpl(
    signature: IdSignature,
    descriptor: TypeParameterDescriptor,
): IrTypeParameterSymbol {
    return IrTypeParameterSymbolImpl(descriptor, signature)
}

abstract class IrBaseRebindablePublicSymbol<out Descriptor : DeclarationDescriptor, Owner : IrSymbolOwner> constructor(
    override val signature: IdSignature,
    descriptor: Descriptor,
) : IrBindableSymbol<Descriptor, Owner> {

    private val _descriptor: Descriptor? = descriptor

    @ObsoleteDescriptorBasedAPI
    @Suppress("UNCHECKED_CAST")
    override val descriptor: Descriptor
        get() = _descriptor ?: (owner as IrDeclaration).toIrBasedDescriptor() as Descriptor

    @ObsoleteDescriptorBasedAPI
    override val hasDescriptor: Boolean
        get() = _descriptor != null

    private var _owner: Owner? = null
    override val owner: Owner
        get() = _owner ?: error("${javaClass.simpleName} is unbound. Signature: $signature")

    override var privateSignature: IdSignature? = null

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
        descriptor is ValueParameterDescriptor && isOriginalDescriptor(descriptor.containingDeclaration) ||
            descriptor == descriptor.original

    override val isBound: Boolean
        get() = _owner != null

    override fun bind(owner: Owner) {
        this._owner = owner
    }

    fun unbind() {
        this._owner = null
    }

    override fun toString(): String {
        if (isBound) return owner.render()
        return if (isPublicApi)
            "Unbound public symbol ${this::class.java.simpleName}: $signature"
        else
            "Unbound private symbol " +
                if (_descriptor != null) "${this::class.java.simpleName}: $_descriptor" else super.toString()
    }
}
