package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameTypesConflictingWithKeywordsPhase : SirPhase {

    private val keywords = setOf(
        "Type",
    )

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allLocalTypes
            .filter { it.simpleName in keywords }
            .forEach {
                it.simpleName += "_"
            }
    }
}
