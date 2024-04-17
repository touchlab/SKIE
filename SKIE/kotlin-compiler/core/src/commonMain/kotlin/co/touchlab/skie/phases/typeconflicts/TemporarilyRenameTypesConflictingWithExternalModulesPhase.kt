package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.module

// Needed for ApiNotes used for the Swift files compilation but not for the final ApiNotes in the framework
// Allows us to not rename types that conflict with external modules
// Must be the last phase that renames SirTypeDeclarations
object TemporarilyRenameTypesConflictingWithExternalModulesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val conflictingModules = sirProvider.allExternalTypeDeclarations.map { it.module.name }
        val conflictingNames = conflictingModules.toMutableSet()

        sirProvider.allLocalTypeDeclarations
            .forEach {
                it.renameConflictingType(conflictingNames)
            }
    }

    context(SirPhase.Context)
    private fun SirTypeDeclaration.renameConflictingType(conflictingNames: MutableSet<String>) {
        registerReverseOperation(this)

        while (fqName.toLocalString() in conflictingNames) {
            this.baseName += "_"
        }

        conflictingNames.add(this.fqName.toLocalString())
    }

    context(SirPhase.Context)
    private fun registerReverseOperation(sirTypeDeclaration: SirTypeDeclaration) {
        val originalBaseName = sirTypeDeclaration.baseName

        doInPhase(RevertPhase) {
            sirTypeDeclaration.baseName = originalBaseName
        }
    }

    object RevertPhase : StatefulSirPhase()
}
