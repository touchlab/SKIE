package co.touchlab.skie.kir.irbuilder

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

interface DeclarationTemplate<D : DeclarationDescriptor> {

    val descriptor: D

    context(MutableDescriptorProvider)
    fun registerExposedDescriptor()

    context(SymbolTablePhase.Context)
    fun declareSymbol()

    context(KotlinIrPhase.Context)
    fun generateIrDeclaration(parent: IrDeclarationContainer, generatorContext: GeneratorContext)

    context(KotlinIrPhase.Context)
    fun generateIrBody()
}
