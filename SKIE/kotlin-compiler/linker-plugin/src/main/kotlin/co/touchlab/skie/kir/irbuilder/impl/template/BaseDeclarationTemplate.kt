@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.irbuilder.impl.template

import co.touchlab.skie.kir.irbuilder.DeclarationTemplate
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.pluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

abstract class BaseDeclarationTemplate<D : DeclarationDescriptor, IR : IrDeclaration, S : IrBindableSymbol<D, IR>> :
    DeclarationTemplate<D> {

    context(KotlinIrPhase.Context)
    override fun generateIrDeclaration(parent: IrDeclarationContainer, generatorContext: GeneratorContext) {
        createDeclarationStubsIfIrLazyClass(parent)

        val syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)

        descriptor.accept(syntheticDeclarationsGenerator, parent)

        val symbol = getSymbol()
        val ir = symbol.owner

        ir.patchDeclarationParents(ir.parent)
    }

    private fun createDeclarationStubsIfIrLazyClass(parent: IrDeclarationContainer) {
        // Instantiates lazy value
        parent.declarations
    }

    context(KotlinIrPhase.Context)
    override fun generateIrBody() {
        val symbol = getSymbol()

        val declarationIrBuilder = DeclarationIrBuilder(pluginContext.generatorContext, symbol, startOffset = 0, endOffset = 0)

        initializeBody(symbol.owner, declarationIrBuilder)
    }

    context(KotlinIrPhase.Context)
    protected abstract fun getSymbol(): S

    context(KotlinIrPhase.Context)
    protected abstract fun initializeBody(declaration: IR, declarationIrBuilder: DeclarationIrBuilder)
}
