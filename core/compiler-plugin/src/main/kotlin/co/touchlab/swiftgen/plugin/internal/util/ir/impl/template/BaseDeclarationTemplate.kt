package co.touchlab.swiftgen.plugin.internal.util.ir.impl.template

import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationTemplate
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

internal abstract class BaseDeclarationTemplate<D : DeclarationDescriptor, IR : IrDeclaration, S : IrBindableSymbol<D, IR>> :
    DeclarationTemplate<D> {

    override fun generateIr(parent: IrDeclarationContainer, generatorContext: GeneratorContext) {
        val syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)

        descriptor.accept(syntheticDeclarationsGenerator, parent)

        val symbol = generatorContext.symbolTable.getSymbol(descriptor)

        val declarationIrBuilder = DeclarationIrBuilder(generatorContext, symbol, startOffset = 0, endOffset = 0)

        symbol.owner.initialize(generatorContext.symbolTable, declarationIrBuilder)
    }

    protected abstract fun ReferenceSymbolTable.getSymbol(descriptor: D): S

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) protected abstract fun IR.initialize() once possible
    protected abstract fun IR.initialize(symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder)
}
