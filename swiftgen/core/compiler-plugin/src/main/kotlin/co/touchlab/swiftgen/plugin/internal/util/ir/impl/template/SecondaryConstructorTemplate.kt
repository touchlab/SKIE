package co.touchlab.swiftgen.plugin.internal.util.ir.impl.template

import co.touchlab.swiftgen.plugin.internal.util.ir.Namespace
import co.touchlab.swiftgen.plugin.internal.util.ir.SecondaryConstructorBuilder
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeclarationDescriptorImplReflector
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
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

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(ReferenceSymbolTable) DeclarationIrBuilder.(IrConstructor) -> IrBody

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

    override fun ReferenceSymbolTable.getSymbol(descriptor: ClassConstructorDescriptor): IrConstructorSymbol =
        referenceConstructor(descriptor)

    override fun IrConstructor.initialize(symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder) {
        body = irBodyBuilder(symbolTable, declarationIrBuilder, this)
    }
}
