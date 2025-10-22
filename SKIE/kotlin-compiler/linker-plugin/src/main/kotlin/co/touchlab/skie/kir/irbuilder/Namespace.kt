package co.touchlab.skie.kir.irbuilder

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement

interface Namespace<D : DeclarationDescriptor> {

    val descriptor: D

    val sourceElement: SourceElement

    context(MutableDescriptorProvider)
    fun addTemplate(declarationTemplate: DeclarationTemplate<*>)

    context(SymbolTablePhase.Context)
    fun registerSymbols()

    context(KotlinIrPhase.Context)
    fun generateIrDeclarations()

    context(KotlinIrPhase.Context)
    fun generateIrBodies()
}
