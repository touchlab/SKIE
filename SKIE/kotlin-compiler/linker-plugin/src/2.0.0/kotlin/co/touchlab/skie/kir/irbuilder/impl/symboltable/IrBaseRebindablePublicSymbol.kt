@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrPublicSymbolBase
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterPublicSymbolImpl as KotlinIrTypeParameterPublicSymbolImpl

fun getIrTypeParameterPublicSymbolImpl(
    signature: IdSignature,
    descriptor: TypeParameterDescriptor,
): IrTypeParameterSymbol {
    return KotlinIrTypeParameterPublicSymbolImpl(signature, descriptor)
}

abstract class IrBaseRebindablePublicSymbol<out Descriptor : DeclarationDescriptor, Owner : IrSymbolOwner>(
    signature: IdSignature,
    descriptor: Descriptor,
) : IrBindableSymbol<Descriptor, Owner>, IrPublicSymbolBase<Descriptor>(signature, descriptor) {

    private var _owner: Owner? = null
    override val owner: Owner
        get() = _owner ?: throw IllegalStateException("Symbol is not bound")

    override fun bind(owner: Owner) {
        this._owner = owner
    }

    fun unbind() {
        this._owner = null
    }

    override val isBound: Boolean
        get() = _owner != null

    override var privateSignature: IdSignature? = null
}
