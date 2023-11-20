package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.resolveCollisionWithWarning

object RenameTypesConflictingWithKeywordsPhase : SirPhase {

    private val problematicKeywords = listOf(
        "Protocol",
        "Self",
        "Type",
    )

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allLocalTypeDeclarations
            .forEach { declaration ->
                declaration.resolveCollisionWithWarning {
                    if (declaration.simpleName in problematicKeywords) "a reserved Swift keyword '${declaration.simpleName}'" else null
                }
            }
    }
}
