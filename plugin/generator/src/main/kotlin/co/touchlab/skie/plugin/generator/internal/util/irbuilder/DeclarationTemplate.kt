package co.touchlab.skie.plugin.generator.internal.util.irbuilder

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

internal interface DeclarationTemplate<D : DeclarationDescriptor> {

    val descriptor: D

    fun declareSymbol(symbolTable: SymbolTable)

    fun generateIr(parent: IrDeclarationContainer, generatorContext: GeneratorContext)
}
