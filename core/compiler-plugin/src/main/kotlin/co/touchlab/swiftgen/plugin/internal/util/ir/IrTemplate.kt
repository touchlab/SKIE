package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

internal interface IrTemplate<D : DeclarationDescriptor, I : IrDeclaration, S : IrBindableSymbol<*, I>> {

    fun createDescriptor(): D

    fun getSymbol(descriptor: D, symbolTable: ReferenceSymbolTable): S

    fun initializeIr(declaration: I, symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder)
}