package co.touchlab.skie.kir.irbuilder.impl.template

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.FunctionBuilder
import co.touchlab.skie.kir.irbuilder.Namespace
import co.touchlab.skie.kir.irbuilder.impl.symboltable.DummyIrSimpleFunction
import co.touchlab.skie.kir.irbuilder.impl.symboltable.IrRebindableSimpleFunctionPublicSymbol
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.Name

class FunctionTemplate(
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

    // TODO Change to context(KotlinIrPhase.Context, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(KotlinIrPhase.Context) DeclarationIrBuilder.(IrSimpleFunction) -> IrBody

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

    context(MutableDescriptorProvider)
    override fun registerExposedDescriptor() {
        this@MutableDescriptorProvider.exposeCallableMember(descriptor)
    }

    context(SymbolTablePhase.Context)
    override fun declareSymbol() {
        val signature = skieSymbolTable.signaturer.composeSignature(descriptor)
            ?: throw IllegalArgumentException("Only exported declarations are currently supported. Check declaration visibility.")

        // IrRebindableSimpleFunctionPublicSymbol is used so that we can later bind it to the correct declaration which cannot be created before the symbol table is validated to not contain any unbound symbols.
        val symbolFactory = { IrRebindableSimpleFunctionPublicSymbol(signature, descriptor) }
        val functionFactory = { symbol: IrSimpleFunctionSymbol ->
            DummyIrSimpleFunction(symbol).also {
                // In 1.8.0 the symbol is already present before calling declareSimpleFunction and therefore is not IrRebindableSimpleFunctionPublicSymbol
                // Starting from 1.9.0 the SymbolTable has additional check that requires that the symbol of created function is bounded in the factory.
                if (symbol is IrRebindableSimpleFunctionPublicSymbol) {
                    symbol.bind(it)
                }
            }
        }

        val declaration = skieSymbolTable.kotlinSymbolTable.declareSimpleFunction(signature, symbolFactory, functionFactory)
        // But the symbol cannot be bounded otherwise DeclarationBuilder will not to generate the declaration (because it thinks it already exists).
        (declaration.symbol as? IrRebindableSimpleFunctionPublicSymbol)?.unbind()
    }

    context(KotlinIrPhase.Context)
    override fun getSymbol(): IrSimpleFunctionSymbol =
        skieSymbolTable.descriptorExtension.referenceSimpleFunction(descriptor)

    context(KotlinIrPhase.Context)
    override fun initializeBody(declaration: IrSimpleFunction, declarationIrBuilder: DeclarationIrBuilder) {
        declaration.body = irBodyBuilder(this@Context, declarationIrBuilder, declaration)
    }
}
