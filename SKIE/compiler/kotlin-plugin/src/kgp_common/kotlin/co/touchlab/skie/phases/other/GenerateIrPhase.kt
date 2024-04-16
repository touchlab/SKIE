package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.KotlinIrPhase

object GenerateIrPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override suspend fun execute() {
        declarationBuilder.generateIr()
    }
}
