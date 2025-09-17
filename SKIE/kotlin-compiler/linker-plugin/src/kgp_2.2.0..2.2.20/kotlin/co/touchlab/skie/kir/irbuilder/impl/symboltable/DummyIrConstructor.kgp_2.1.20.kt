package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

actual class DummyIrConstructor actual constructor(override val symbol: IrConstructorSymbol) : IrConstructor() {

    actual override var isPrimary: Boolean by unsupported()
    override var startOffset: Int by unsupported()
    override var endOffset: Int by unsupported()
    override val factory: IrFactory by unsupported()
    override var origin: IrDeclarationOrigin by unsupported()
    override var name: Name by unsupported()
    override var visibility: DescriptorVisibility by unsupported()
    override var body: IrBody? by unsupported()

    @ObsoleteDescriptorBasedAPI
    override val descriptor: ClassConstructorDescriptor by unsupported()
    actual override var isExpect: Boolean by unsupported()
    actual override var isInline: Boolean by unsupported()
    override var returnType: IrType by unsupported()
    override val containerSource: DeserializedContainerSource? by unsupported()
    override var metadata: MetadataSource? by unsupported()
    override var annotations: List<IrConstructorCall> by unsupported()
    actual override var isExternal: Boolean by unsupported()
    override var typeParameters: List<IrTypeParameter> by unsupported()
    override var attributeOwnerId: IrElement by unsupported()
}
