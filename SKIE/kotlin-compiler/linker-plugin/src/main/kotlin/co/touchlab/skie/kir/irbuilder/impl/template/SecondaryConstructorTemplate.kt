package co.touchlab.skie.kir.irbuilder.impl.template

import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.DeclarationDescriptorImplReflector
import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.Namespace
import co.touchlab.skie.kir.irbuilder.SecondaryConstructorBuilder
import co.touchlab.skie.kir.irbuilder.impl.symboltable.DummyIrConstructor
import co.touchlab.skie.kir.irbuilder.impl.symboltable.IrRebindableConstructorPublicSymbol
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import co.touchlab.skie.phases.skieSymbolTable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.name.Name

class SecondaryConstructorTemplate(
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

    // TODO Change to context(KotlinIrPhase.Context, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(KotlinIrPhase.Context) DeclarationIrBuilder.(IrConstructor) -> IrBody

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

    context(MutableDescriptorProvider)
    override fun registerExposedDescriptor() {
        this@MutableDescriptorProvider.exposeCallableMember(descriptor)
    }

    context(SymbolTablePhase.Context)
    override fun declareSymbol() {
        val signature = skieSymbolTable.signaturer.composeSignature(descriptor)
            ?: throw IllegalArgumentException("Only exported declarations are currently supported. Check declaration visibility.")

        // IrRebindableConstructorPublicSymbol is used so that we can later bind it to the correct declaration which cannot be created before the symbol table is validated to not contain any unbound symbols.
        val symbolFactory = { IrRebindableConstructorPublicSymbol(signature, descriptor) }
        val functionFactory = { symbol: IrConstructorSymbol ->
            DummyIrConstructor(symbol).also {
                if (symbol is IrRebindableConstructorPublicSymbol) {
                    symbol.bind(it)
                }
            }
        }

        val declaration = skieSymbolTable.kotlinSymbolTable.declareConstructor(signature, symbolFactory, functionFactory)
        // But the symbol cannot be bounded otherwise DeclarationBuilder will not to generate the declaration (because it thinks it already exists).
        (declaration.symbol as? IrRebindableConstructorPublicSymbol)?.unbind()
    }

    context(KotlinIrPhase.Context)
    override fun getSymbol(): IrConstructorSymbol =
        skieSymbolTable.descriptorExtension.referenceConstructor(descriptor)

    context(KotlinIrPhase.Context)
    override fun initializeBody(declaration: IrConstructor, declarationIrBuilder: DeclarationIrBuilder) {
        declaration.body = irBodyBuilder(this@Context, declarationIrBuilder, declaration)
    }
}
