package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrPublicSymbolBase
import org.jetbrains.kotlin.ir.util.IdSignature

internal abstract class IrBaseRebindablePublicSymbol<out D : DeclarationDescriptor, B : IrSymbolOwner>(
    signature: IdSignature,
    descriptor: D,
) : IrBindableSymbol<D, B>, IrPublicSymbolBase<D>(signature, descriptor) {

    override lateinit var owner: B

    override fun bind(owner: B) {
        this.owner = owner
    }

    override val isBound: Boolean
        get() = ::owner.isInitialized

    override var privateSignature: IdSignature? = null
}
