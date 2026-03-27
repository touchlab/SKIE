package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.phases.SirPhase

object VerifyTestPhasesAreExecutedPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val shouldCrash = kirProvider.kotlinClasses.any { it.swiftName == "VerifySkieTestPhasesAreExecuted" }

        if (shouldCrash) {
            error("Test phases are executed.")
        }
    }
}
