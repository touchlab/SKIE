package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

expect fun IrTypeParameterPublicSymbolImpl(
    signature: IdSignature,
    descriptor: TypeParameterDescriptor,
): IrTypeParameterSymbol

expect abstract class IrBaseRebindablePublicSymbol<out Descriptor : DeclarationDescriptor, Owner : IrSymbolOwner>(
    signature: IdSignature,
    descriptor: Descriptor,
) : IrBindableSymbol<Descriptor, Owner> {
    override val signature: IdSignature

    @ObsoleteDescriptorBasedAPI
    override val descriptor: Descriptor

    @ObsoleteDescriptorBasedAPI
    override val hasDescriptor: Boolean

    override val owner: Owner

    override var privateSignature: IdSignature?

    override val isBound: Boolean

    override fun bind(owner: Owner)

    fun unbind()
}
