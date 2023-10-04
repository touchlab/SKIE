package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import io.outfoxx.swiftpoet.KEYWORDS

// TODO We should tests which keywords cause problems because it's not all of them
object RenameNestedKotlinTypesConflictingWithKeywordsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        swiftModelProvider.exposedClasses
            .map { it.kotlinSirClass }
            .filter { it.namespace?.fqName?.simpleName in KEYWORDS }
            .forEach {
                it.baseName = it.fqName.toString().replace(".", "")
                it.namespace = null
            }
    }
}
