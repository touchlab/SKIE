package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

internal interface DeclarationBuilder<D : DeclarationDescriptor, I : IrDeclaration, S : IrBindableSymbol<*, I>> {

    fun createDescriptor(containingDeclarationDescriptor: DeclarationDescriptor, sourceElement: SourceElement): D

    fun getSymbol(descriptor: D, symbolTable: ReferenceSymbolTable): S

    fun initializeIr(declaration: I, symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder)
}