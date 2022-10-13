package co.touchlab.swiftgen.plugin.internal.util.ir.impl.template

import co.touchlab.swiftgen.plugin.internal.util.ir.FunctionBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.Namespace
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
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

    override fun ReferenceSymbolTable.getSymbol(descriptor: FunctionDescriptor): IrSimpleFunctionSymbol =
        referenceSimpleFunction(descriptor)

    override fun IrSimpleFunction.initialize(symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder) {
        body = irBodyBuilder(symbolTable, declarationIrBuilder, this)
    }
}
