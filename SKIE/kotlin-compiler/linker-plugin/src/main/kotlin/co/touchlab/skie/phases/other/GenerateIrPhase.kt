package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.declarationBuilder

object GenerateIrPhase : KotlinIrPhase {

    context(context: KotlinIrPhase.Context)
    override suspend fun execute() {
        context.declarationBuilder.generateIr()
    }
}
