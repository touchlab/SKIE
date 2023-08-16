package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.api.phases.util.ExternalTypesProvider
import co.touchlab.skie.plugin.api.model.type.fqIdentifier

class RenameInaccessibleNestedDeclarationsPhase(
    private val skieModule: DefaultSkieModule,
    private val externalTypesProvider: ExternalTypesProvider,
) : SkieLinkingPhase {

    override fun execute() {
        val conflictingNames = externalTypesProvider.allReferencedExternalTypesWithoutBuiltInModules.map { it.name }.toSet()

        skieModule.configure {
            exposedClasses
                .filter { it.containingType?.fqIdentifier in conflictingNames }
                .forEach {
                    it.identifier = it.fqIdentifier.replace(".", "")
                    it.containingType = null
                }
        }
    }
}
