package co.touchlab.skie.kir.irbuilder.impl

import co.touchlab.skie.phases.KotlinIrPhase
import org.jetbrains.kotlin.ir.util.SymbolTable

object GenerateIrPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override fun execute() {
        declarationBuilder.generateIr(moduleFragment, pluginContext, pluginContext.symbolTable as SymbolTable)
    }
}
