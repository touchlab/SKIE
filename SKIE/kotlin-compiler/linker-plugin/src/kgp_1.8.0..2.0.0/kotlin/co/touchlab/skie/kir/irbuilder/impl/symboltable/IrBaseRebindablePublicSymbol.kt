package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.symbols.impl.IrPublicSymbolBase
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterPublicSymbolImpl as KotlinIrTypeParameterPublicSymbolImpl

actual fun IrTypeParameterPublicSymbolImpl(
    signature: IdSignature,
    descriptor: TypeParameterDescriptor,
): IrTypeParameterSymbol {
    return KotlinIrTypeParameterPublicSymbolImpl(signature, descriptor)
}

actual abstract class IrBaseRebindablePublicSymbol<out Descriptor : DeclarationDescriptor, Owner : IrSymbolOwner> actual constructor(
    signature: IdSignature,
    descriptor: Descriptor,
) : IrBindableSymbol<Descriptor, Owner>, IrPublicSymbolBase<Descriptor>(signature, descriptor) {

    private var _owner: Owner? = null
    actual override val owner: Owner
        get() = _owner ?: throw IllegalStateException("Symbol is not bound")

    actual override fun bind(owner: Owner) {
        this._owner = owner
    }

    actual fun unbind() {
        this._owner = null
    }

    actual override val isBound: Boolean
        get() = _owner != null

    actual override var privateSignature: IdSignature? = null
}
