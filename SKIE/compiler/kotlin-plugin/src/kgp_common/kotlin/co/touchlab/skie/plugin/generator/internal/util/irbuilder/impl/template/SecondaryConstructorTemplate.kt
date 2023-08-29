package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.SecondaryConstructorBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.DummyIrConstructor
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.IrRebindableConstructorPublicSymbol
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.DeclarationDescriptorImplReflector
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.Name

internal class SecondaryConstructorTemplate(
    name: Name,
    namespace: Namespace<ClassDescriptor>,
    annotations: Annotations,
    config: SecondaryConstructorBuilder.() -> Unit,
) : BaseDeclarationTemplate<ClassConstructorDescriptor, IrConstructor, IrConstructorSymbol>() {

    override val descriptor: ClassConstructorDescriptorImpl = ClassConstructorDescriptorImpl.create(
        namespace.descriptor,
        annotations,
        false,
        namespace.sourceElement,
    )

    private val constructorBuilder = SecondaryConstructorBuilder(descriptor)

    // TODO Change to context(IrPluginContext, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(IrPluginContext) DeclarationIrBuilder.(IrConstructor) -> IrBody

    init {
        descriptor.reflectedBy<DeclarationDescriptorImplReflector>().name = name

        constructorBuilder.config()

        irBodyBuilder = constructorBuilder.body ?: throw IllegalStateException("Constructor must have a body.")

        descriptor.initialize(
            constructorBuilder.valueParameters,
            constructorBuilder.visibility,
        )

        descriptor.returnType = namespace.descriptor.defaultType
    }

    override fun declareSymbol(symbolTable: SymbolTable) {
        val signature = symbolTable.signaturer.composeSignature(descriptor)
            ?: throw IllegalArgumentException("Only exported declarations are currently supported. Check declaration visibility.")

        // IrRebindableConstructorPublicSymbol is used so that we can later bind it to the correct declaration which cannot be created before the symbol table is validated to not contain any unbound symbols.
        val symbolFactory = { IrRebindableConstructorPublicSymbol(signature, descriptor) }
        val functionFactory = { symbol: IrConstructorSymbol ->
            DummyIrConstructor(symbol).also {
                // In 1.8.0 the symbol is already present before calling declareConstructor and therefore is not IrRebindableConstructorPublicSymbol
                // Starting from 1.9.0 the SymbolTable has additional check that requires that the symbol of created function is bounded in the factory.
                if (symbol is IrRebindableConstructorPublicSymbol) {
                    symbol.bind(it)
                }
            }
        }

        val declaration = symbolTable.declareConstructor(signature, symbolFactory, functionFactory)
        // But the symbol cannot be bounded otherwise DeclarationBuilder will not to generate the declaration (because it thinks it already exists).
        (declaration.symbol as? IrRebindableConstructorPublicSymbol)?.unbind()
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun getSymbol(symbolTable: ReferenceSymbolTable): IrConstructorSymbol =
        symbolTable.referenceConstructor(descriptor)

    override fun initializeBody(
        declaration: IrConstructor,
        irPluginContext: IrPluginContext,
        declarationIrBuilder: DeclarationIrBuilder,
    ) {
        declaration.body = irBodyBuilder(irPluginContext, declarationIrBuilder, declaration)
    }
}
