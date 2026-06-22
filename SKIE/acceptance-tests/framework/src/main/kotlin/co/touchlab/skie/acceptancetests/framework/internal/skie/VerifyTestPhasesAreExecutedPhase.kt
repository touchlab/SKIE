package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.phases.SirPhase

object VerifyTestPhasesAreExecutedPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        val shouldCrash = context.kirProvider.kotlinClasses.any { it.swiftName == "VerifySkieTestPhasesAreExecuted" }

        if (shouldCrash) {
            error("Test phases are executed.")
        }
    }
}
