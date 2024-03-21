package co.touchlab.skie.kir.irbuilder.impl

import co.touchlab.skie.phases.KotlinIrPhase

object GenerateIrPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override suspend fun execute() {
        declarationBuilder.generateIr()
    }
}
