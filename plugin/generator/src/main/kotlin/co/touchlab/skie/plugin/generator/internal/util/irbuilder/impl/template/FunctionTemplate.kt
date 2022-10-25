package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.FunctionBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.DummyIrSimpleFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.IrRebindableSimpleFunctionPublicSymbol
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.Name

internal class FunctionTemplate(
    name: Name,
    namespace: Namespace<*>,
    annotations: Annotations,
    config: FunctionBuilder.() -> Unit,
) : BaseDeclarationTemplate<FunctionDescriptor, IrSimpleFunction, IrSimpleFunctionSymbol>() {

    override val descriptor: SimpleFunctionDescriptorImpl = SimpleFunctionDescriptorImpl.create(
        namespace.descriptor,
        annotations,
        name,
        CallableMemberDescriptor.Kind.SYNTHESIZED,
        namespace.sourceElement,
    )

    private val functionBuilder = FunctionBuilder(descriptor)

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(ReferenceSymbolTable) DeclarationIrBuilder.(IrSimpleFunction) -> IrBody

    init {
        functionBuilder.config()

        irBodyBuilder = functionBuilder.body ?: throw IllegalStateException("Function must have a body.")

        descriptor.initialize(
            functionBuilder.extensionReceiverParameter,
            functionBuilder.dispatchReceiverParameter,
            functionBuilder.contextReceiverParameters,
            functionBuilder.typeParameters,
            functionBuilder.valueParameters,
            functionBuilder.returnType,
            functionBuilder.modality,
            functionBuilder.visibility,
        )

        descriptor.isInline = functionBuilder.isInline
        descriptor.isSuspend = functionBuilder.isSuspend
    }

    override fun declareSymbol(symbolTable: SymbolTable) {
        val signature = symbolTable.signaturer.composeSignature(descriptor)
            ?: throw IllegalArgumentException("Only exported declarations are currently supported. Check declaration visibility.")

        val symbolFactory = { IrRebindableSimpleFunctionPublicSymbol(signature, descriptor) }

        symbolTable.declareSimpleFunction(signature, symbolFactory, ::DummyIrSimpleFunction)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun getSymbol(symbolTable: ReferenceSymbolTable): IrSimpleFunctionSymbol =
        symbolTable.referenceSimpleFunction(descriptor)

    override fun IrSimpleFunction.initialize(symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder) {
        body = irBodyBuilder(symbolTable, declarationIrBuilder, this)
    }
}
