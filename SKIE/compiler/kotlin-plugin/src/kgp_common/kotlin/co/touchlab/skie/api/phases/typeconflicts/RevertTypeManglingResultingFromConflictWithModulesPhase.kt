package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.module.SkieModule

class RevertTypeManglingResultingFromConflictWithModulesPhase(
    private val skieModule: SkieModule,
    private val context: MangleTypesConflictingWithModulesPhase.Context,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure {
            context.reverseOperations.forEach {
                it.invoke()
            }
        }
    }
}
