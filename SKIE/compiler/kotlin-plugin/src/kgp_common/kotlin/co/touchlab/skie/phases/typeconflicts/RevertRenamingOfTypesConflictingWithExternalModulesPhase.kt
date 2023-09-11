package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.phases.SkieModule

class RevertRenamingOfTypesConflictingWithExternalModulesPhase(
    private val skieModule: SkieModule,
    private val context: TemporarilyRenameTypesConflictingWithExternalModulesPhase.Context,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure {
            context.reverseOperations.forEach {
                it.invoke()
            }
        }
    }
}
