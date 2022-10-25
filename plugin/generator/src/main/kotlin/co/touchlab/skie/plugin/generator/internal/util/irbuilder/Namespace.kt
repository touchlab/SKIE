package co.touchlab.skie.plugin.generator.internal.util.irbuilder

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.util.SymbolTable

internal interface Namespace<D : DeclarationDescriptor> {

    val descriptor: D

    val sourceElement: SourceElement

    fun addTemplate(declarationTemplate: DeclarationTemplate<*>, symbolTable: SymbolTable)

    fun generateIr(pluginContext: IrPluginContext, symbolTable: SymbolTable)
}
