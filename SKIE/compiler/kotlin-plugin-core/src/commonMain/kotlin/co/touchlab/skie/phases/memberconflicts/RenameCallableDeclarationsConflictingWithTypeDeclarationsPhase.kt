package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.util.resolveCollisionWithWarning

// TODO This does not work for nested classes
object RenameCallableDeclarationsConflictingWithTypeDeclarationsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val topLevelDeclarations = sirProvider.allSkieGeneratedTopLevelDeclarations

        val topLevelClasses = topLevelDeclarations.filterIsInstance<SirClass>()
        val reservedNames = topLevelClasses.map { it.simpleName }.toSet()

        val globalCallableDeclarations = topLevelDeclarations.filterIsInstance<SirCallableDeclaration>()

        globalCallableDeclarations.forEach {
            it.renameIfConflictsWith(reservedNames)
        }
    }

    context(SirPhase.Context)
    private fun SirCallableDeclaration.renameIfConflictsWith(reservedNames: Set<String>) {
        this.resolveCollisionWithWarning {
            if (identifierAfterVisibilityChange in reservedNames) "a type name '$identifierAfterVisibilityChange'" else null
        }
    }
}
