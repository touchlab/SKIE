package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameNestedTypesConflictingWithExternalTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val conflictingNames = sirProvider.allExternalTypeDeclarations.map { it.simpleName }

        sirProvider.allLocalTypeDeclarations
            .filter { it.namespace?.fqName?.toString() in conflictingNames }
            .forEach {
                it.baseName = it.fqName.toString().replace(".", "")
                it.namespace = null
            }
    }
}
