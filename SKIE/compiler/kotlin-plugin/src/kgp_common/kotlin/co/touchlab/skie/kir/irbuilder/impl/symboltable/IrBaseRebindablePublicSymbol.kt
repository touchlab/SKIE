package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrPublicSymbolBase
import org.jetbrains.kotlin.ir.util.IdSignature

internal abstract class IrBaseRebindablePublicSymbol<out D : DeclarationDescriptor, B : IrSymbolOwner>(
    signature: IdSignature,
    descriptor: D,
) : IrBindableSymbol<D, B>, IrPublicSymbolBase<D>(signature, descriptor) {

    private var _owner: B? = null
    override val owner: B
        get() = _owner ?: throw IllegalStateException("Symbol is not bound")

    override fun bind(owner: B) {
        this._owner = owner
    }

    fun unbind() {
        this._owner = null
    }

    override val isBound: Boolean
        get() = _owner != null

    override var privateSignature: IdSignature? = null
}
