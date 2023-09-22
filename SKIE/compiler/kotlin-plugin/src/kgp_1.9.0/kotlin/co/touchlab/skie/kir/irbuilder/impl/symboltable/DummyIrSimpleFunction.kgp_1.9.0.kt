package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

actual class DummyIrSimpleFunction actual constructor(
    override val symbol: IrSimpleFunctionSymbol,
) : IrSimpleFunction() {

    override val startOffset: Int by unsupported()
    override val endOffset: Int by unsupported()
    override var attributeOwnerId: IrAttributeContainer by unsupported()
    override val factory: IrFactory by unsupported()
    override var origin: IrDeclarationOrigin by unsupported()
    override var originalBeforeInline: IrAttributeContainer? by unsupported()
    override var parent: IrDeclarationParent by unsupported()
    override var name: Name by unsupported()
    override var visibility: DescriptorVisibility by unsupported()
    override var body: IrBody? by unsupported()
    override var contextReceiverParametersCount: Int by unsupported()

    @ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor by unsupported()
    override var dispatchReceiverParameter: IrValueParameter? by unsupported()
    override var extensionReceiverParameter: IrValueParameter? by unsupported()
    override var isExpect: Boolean by unsupported()
    override var isInline: Boolean by unsupported()
    override var returnType: IrType by unsupported()
    override var valueParameters: List<IrValueParameter> by unsupported()
    override val containerSource: DeserializedContainerSource? by unsupported()
    override var metadata: MetadataSource? by unsupported()
    override var annotations: List<IrConstructorCall> by unsupported()
    override var overriddenSymbols: List<IrSimpleFunctionSymbol> by unsupported()
    override var modality: Modality by unsupported()
    override var isExternal: Boolean by unsupported()
    override var correspondingPropertySymbol: IrPropertySymbol? by unsupported()
    override var isFakeOverride: Boolean by unsupported()
    override var isInfix: Boolean by unsupported()
    override var isOperator: Boolean by unsupported()
    override var isSuspend: Boolean by unsupported()
    override var isTailrec: Boolean by unsupported()
    override var typeParameters: List<IrTypeParameter> by unsupported()
}
