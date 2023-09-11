package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.util.Reporter

class RenameTypesConflictingWithKotlinModulePhase(
    private val skieModule: SkieModule,
    private val reporter: Reporter,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.First) {
            fixTypeNameCollisions()
        }
    }

    private fun MutableSwiftModelScope.fixTypeNameCollisions() {
        val moduleName = sirBuiltins.Kotlin.module.name

        var collisionExists = false

        sirProvider.allLocalPublicTypes.forEach { type ->
            if (type.fqName.toString() == moduleName) {
                type.simpleName += "_"
                collisionExists = true
            }
        }

        if (collisionExists) {
            logModuleNameCollisionWarning(moduleName)
        }
    }

    private fun logModuleNameCollisionWarning(moduleName: String) {
        reporter.warning(
            "Type '$moduleName' was renamed to '${moduleName}_' " +
                "because it has the same name as the produced framework which is forbidden.",
        )
    }
}
