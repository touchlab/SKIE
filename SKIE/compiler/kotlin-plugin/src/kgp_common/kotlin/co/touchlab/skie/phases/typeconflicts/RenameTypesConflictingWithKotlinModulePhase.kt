package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase

object RenameTypesConflictingWithKotlinModulePhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
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

    context(SirPhase.Context)
    private fun logModuleNameCollisionWarning(moduleName: String) {
        reporter.warning(
            "Type '$moduleName' was renamed to '${moduleName}_' " +
                "because it has the same name as the produced framework which is forbidden.",
        )
    }
}
