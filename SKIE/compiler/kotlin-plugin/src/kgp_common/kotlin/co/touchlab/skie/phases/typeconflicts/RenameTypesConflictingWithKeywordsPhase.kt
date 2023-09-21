package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import io.outfoxx.swiftpoet.KEYWORDS

object RenameTypesConflictingWithKeywordsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allLocalTypes
            .filter { it.simpleName in KEYWORDS }
            .forEach {
                it.simpleName += "_"
            }
    }
}
