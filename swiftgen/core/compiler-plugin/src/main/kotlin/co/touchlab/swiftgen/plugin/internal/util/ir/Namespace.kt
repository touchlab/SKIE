package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.util.SymbolTable

internal interface Namespace<D : DeclarationDescriptor> {

    val descriptor: D

    val sourceElement: SourceElement

    val declarations: List<DeclarationDescriptor>

    fun addTemplate(declarationTemplate: DeclarationTemplate<*>)

    fun generateIr(pluginContext: IrPluginContext, symbolTable: SymbolTable)
}
