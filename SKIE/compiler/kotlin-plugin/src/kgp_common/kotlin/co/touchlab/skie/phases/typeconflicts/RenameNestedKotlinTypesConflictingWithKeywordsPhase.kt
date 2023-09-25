package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import io.outfoxx.swiftpoet.KEYWORDS

object RenameNestedKotlinTypesConflictingWithKeywordsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        swiftModelProvider.exposedClasses
            .map { it.kotlinSirClass }
            .filter { it.namespace?.fqName?.simpleName in KEYWORDS }
            .forEach {
                it.simpleName = it.fqName.toString().replace(".", "")
                it.namespace = null
            }
    }
}
