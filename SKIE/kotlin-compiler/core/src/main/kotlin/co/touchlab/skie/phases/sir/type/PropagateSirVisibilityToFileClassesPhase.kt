package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationWithVisibility
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively
import co.touchlab.skie.sir.element.kirClassOrNull
import co.touchlab.skie.sir.element.maximumVisibility

object PropagateSirVisibilityToFileClassesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalClasses
            .filter { it.kirClassOrNull?.kind == KirClass.Kind.File }
            .forEach {
                propagateVisibilityFromMembers(it)
            }
    }

    private fun propagateVisibilityFromMembers(sirClass: SirClass) {
        sirClass.visibility = sirClass.getAllDeclarationsRecursively()
            .filterIsInstance<SirDeclarationWithVisibility>()
            .map { it.visibility }
            .maximumVisibility()
    }
}
