package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.util.collisionFreeIdentifier

object RenameParametersNamedSelfPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allSkieGeneratedSimpleFunctions
            .forEach {
                renameParametersNamedSelf(it)
            }
    }

    private fun renameParametersNamedSelf(function: SirSimpleFunction) {
        function.valueParameters
            .filter { it.name == "self" }
            .forEach { parameter ->
                val existingNames = function.valueParameters.map { it.name }

                parameter.name = "self".collisionFreeIdentifier(existingNames)
                parameter.label = parameter.label ?: "self"
            }
    }
}
