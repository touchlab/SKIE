package co.touchlab.skie.kir.irbuilder.impl.template

import co.touchlab.skie.kir.irbuilder.DeclarationTemplate
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

abstract class BaseDeclarationTemplate<D : DeclarationDescriptor, IR : IrDeclaration, S : IrBindableSymbol<D, IR>> :
    DeclarationTemplate<D> {

    override fun generateIrDeclaration(parent: IrDeclarationContainer, generatorContext: GeneratorContext) {
        createDeclarationStubsIfIrLazyClass(parent)

        val syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)

        descriptor.accept(syntheticDeclarationsGenerator, parent)

        val symbol = getSymbol(generatorContext.symbolTable)
        val ir = symbol.owner

        ir.patchDeclarationParents(ir.parent)
    }

    private fun createDeclarationStubsIfIrLazyClass(parent: IrDeclarationContainer) {
        // Instantiates lazy value
        parent.declarations
    }

    override fun generateIrBody(irPluginContext: IrPluginContext) {
        val symbol = getSymbol(irPluginContext.symbolTable)

        val declarationIrBuilder = DeclarationIrBuilder(irPluginContext, symbol, startOffset = 0, endOffset = 0)

        initializeBody(symbol.owner, irPluginContext, declarationIrBuilder)
    }

    protected abstract fun getSymbol(symbolTable: ReferenceSymbolTable): S

    protected abstract fun initializeBody(declaration: IR, irPluginContext: IrPluginContext, declarationIrBuilder: DeclarationIrBuilder)
}
