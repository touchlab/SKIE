package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

class DummyIrConstructor(override val symbol: IrConstructorSymbol) : IrConstructor() {

    override var isPrimary: Boolean by unsupported()
    override val startOffset: Int by unsupported()
    override val endOffset: Int by unsupported()
    override val factory: IrFactory by unsupported()
    override var origin: IrDeclarationOrigin by unsupported()
    override var parent: IrDeclarationParent by unsupported()
    override var name: Name by unsupported()
    override var visibility: DescriptorVisibility by unsupported()
    override var body: IrBody? by unsupported()
    override var contextReceiverParametersCount: Int by unsupported()

    @ObsoleteDescriptorBasedAPI
    override val descriptor: ClassConstructorDescriptor by unsupported()
    override var dispatchReceiverParameter: IrValueParameter? by unsupported()
    override var extensionReceiverParameter: IrValueParameter? by unsupported()
    override var isExpect: Boolean by unsupported()
    override var isInline: Boolean by unsupported()
    override var returnType: IrType by unsupported()
    override var valueParameters: List<IrValueParameter> by unsupported()
    override val containerSource: DeserializedContainerSource? by unsupported()
    override var metadata: MetadataSource? by unsupported()
    override var annotations: List<IrConstructorCall> by unsupported()
    override var isExternal: Boolean by unsupported()
    override var typeParameters: List<IrTypeParameter> by unsupported()
}
