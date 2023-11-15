package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameTypesConflictingWithKeywordsPhase : SirPhase {

    private val problematicKeywords = listOf(
        "Protocol",
        "Self",
        "Type",
    )

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allLocalTypeDeclarations
            .filter { it.simpleName in problematicKeywords }
            .forEach {
                it.baseName += "_"
            }
    }
}
