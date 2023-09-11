package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.phases.SkieModule
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.module

// Needed for ApiNotes used for the Swift files compilation but not for the final ApiNotes in the framework
// Must be the last phase that renames SirTypeDeclarations
class TemporarilyRenameTypesConflictingWithExternalModulesPhase(
    private val skieModule: SkieModule,
    private val context: Context,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure {
            renameConflictingTypes()
        }
    }

    private fun MutableSwiftModelScope.renameConflictingTypes() {
        val conflictingModules = sirProvider.allExternalTypes.map { it.module.name }
        val conflictingNames = conflictingModules.toMutableSet()

        sirProvider.allLocalTypes
            .forEach {
                it.renameConflictingType(conflictingNames)
            }
    }

    private fun SirTypeDeclaration.renameConflictingType(conflictingNames: MutableSet<String>) {
        registerReverseOperation(this)

        while (fqName.toLocalUnescapedNameString() in conflictingNames) {
            this.simpleName += "_"
        }

        conflictingNames.add(this.fqName.toLocalUnescapedNameString())
    }

    private fun registerReverseOperation(sirTypeDeclaration: SirTypeDeclaration) {
        val originalSimpleName = sirTypeDeclaration.simpleName

        context.reverseOperations.add {
            sirTypeDeclaration.simpleName = originalSimpleName
        }
    }

    class Context {

        internal val reverseOperations = mutableListOf<() -> Unit>()
    }
}
