package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameNestedTypesConflictingWithExternalTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val conflictingNames = headerDeclarationsProvider.externalTypes.map { it.name }.toSet()

        sirProvider.allLocalTypes
            .filter { it.namespace?.fqName?.toString() in conflictingNames }
            .forEach {
                it.baseName = it.fqName.toString().replace(".", "")
                it.namespace = null
            }
    }
}
