package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction

// TODO This does not work for nested classes
object RenameCallableDeclarationsConflictingWithTypeDeclarationsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val topLevelDeclarations = sirProvider.allSkieGeneratedTopLevelDeclarations

        val topLevelClasses = topLevelDeclarations.filterIsInstance<SirClass>()
        val reservedNames = topLevelClasses.map { it.simpleName }.toSet()

        val globalCallableDeclarations = topLevelDeclarations.filterIsInstance<SirCallableDeclaration>()

        globalCallableDeclarations.forEach {
            it.renameIfConflictsWith(reservedNames)
        }
    }

    private fun SirCallableDeclaration.renameIfConflictsWith(reservedNames: Set<String>) {
        if (this.identifier !in reservedNames) {
            return
        }

        when (this) {
            is SirConstructor -> error("Constructors cannot be in a global scope: $this")
            is SirSimpleFunction -> this.identifier += "_"
            is SirProperty -> this.identifier += "_"
        }
    }
}
