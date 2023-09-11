package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.DefaultSkieModule
import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.phases.SkieModule

class RenameNestedTypesConflictingWithExternalTypesPhase(
    private val skieModule: DefaultSkieModule,
) : SkieLinkingPhase {

    // WIP 2 Test Any and others
    private val builtInConflictingNames =
        listOf(
            "Protocol",
        )

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.First) {
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
}
