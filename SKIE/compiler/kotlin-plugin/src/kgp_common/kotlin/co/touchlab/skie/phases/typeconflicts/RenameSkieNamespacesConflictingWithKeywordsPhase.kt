package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import io.outfoxx.swiftpoet.KEYWORDS

object RenameSkieNamespacesConflictingWithKeywordsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        swiftModelProvider.exposedTypes
            .map { sirProvider.getSkieNamespace(it) }
            .filter { it.simpleName in KEYWORDS }
            .forEach {
                it.simpleName += "_"
            }
    }
}
