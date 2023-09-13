package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameNestedTypesConflictingWithExternalTypesPhase : SirPhase {

    // WIP 2 Test Any and others and if Protocol is a problem
    private val builtInConflictingNames =
        listOf(
            "Protocol",
        )

    context(SirPhase.Context)
    override fun execute() {
        val conflictingNames = sirProvider.allExternalTypesFromNonBuiltinModules
            .map { it.fqName.toLocalUnescapedNameString() }
            .toSet() + builtInConflictingNames

        sirProvider.allLocalTypes
            .filter { it.namespace?.fqName?.toString() in conflictingNames }
            .forEach {
                it.simpleName = it.fqName.toString().replace(".", "")
                it.namespace = null
            }
    }
}
