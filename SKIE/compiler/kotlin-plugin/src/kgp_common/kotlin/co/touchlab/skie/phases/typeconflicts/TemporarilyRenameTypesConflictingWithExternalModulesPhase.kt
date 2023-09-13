package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.module

// Needed for ApiNotes used for the Swift files compilation but not for the final ApiNotes in the framework
// Must be the last phase that renames SirTypeDeclarations
object TemporarilyRenameTypesConflictingWithExternalModulesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val conflictingModules = sirProvider.allExternalTypes.map { it.module.name }
        val conflictingNames = conflictingModules.toMutableSet()

        sirProvider.allLocalTypes
            .forEach {
                it.renameConflictingType(conflictingNames)
            }
    }

    context(SirPhase.Context)
    private fun SirTypeDeclaration.renameConflictingType(conflictingNames: MutableSet<String>) {
        registerReverseOperation(this)

        while (fqName.toLocalUnescapedNameString() in conflictingNames) {
            this.simpleName += "_"
        }

        conflictingNames.add(this.fqName.toLocalUnescapedNameString())
    }

    context(SirPhase.Context)
    private fun registerReverseOperation(sirTypeDeclaration: SirTypeDeclaration) {
        val originalSimpleName = sirTypeDeclaration.simpleName

        doInPhase(RevertPhase) {
            sirTypeDeclaration.simpleName = originalSimpleName
        }
    }

    object RevertPhase : StatefulSirPhase()
}
