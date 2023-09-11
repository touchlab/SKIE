package co.touchlab.skie.kir.irbuilder

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

internal interface DeclarationTemplate<D : DeclarationDescriptor> {

    val descriptor: D

    fun declareSymbol(symbolTable: SymbolTable)

    fun generateIrDeclaration(parent: IrDeclarationContainer, generatorContext: GeneratorContext)

    fun generateIrBody(irPluginContext: IrPluginContext)
}
