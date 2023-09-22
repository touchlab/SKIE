package co.touchlab.skie.kir.irbuilder

import co.touchlab.skie.kir.DescriptorRegistrationScope
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.util.SymbolTable

interface Namespace<D : DeclarationDescriptor> {

    val descriptor: D

    val sourceElement: SourceElement

    context(DescriptorRegistrationScope)
    fun addTemplate(declarationTemplate: DeclarationTemplate<*>)

    fun registerSymbols(symbolTable: SymbolTable)

    fun generateIrDeclarations(pluginContext: IrPluginContext, symbolTable: SymbolTable)

    fun generateIrBodies(pluginContext: IrPluginContext)
}