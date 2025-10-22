package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirOverridableDeclaration
import co.touchlab.skie.sir.element.SirVisibility

object VerifySirVisibilityInAbstractMembersPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalCallableDeclarations
            .filterIsInstance<SirOverridableDeclaration<*>>()
            .filter { it.isAbstract }
            .forEach {
                updateVisibility(it)
            }
    }

    context(SirPhase.Context)
    private fun updateVisibility(sirOverridableDeclaration: SirOverridableDeclaration<*>) {
        val ownerVisibility = sirOverridableDeclaration.memberOwner?.visibility ?: return

        if (sirOverridableDeclaration.visibility >= ownerVisibility || ownerVisibility == SirVisibility.Private) {
            return
        }

        kirReporter.warning(
            "Abstract members should have at least the same SkieVisibility as parent otherwise the parent cannot be safely inherited from in Swift.",
            sirOverridableDeclaration.kirCallableDeclarationOrNull,
        )
    }

    context(SirPhase.Context)
    private val SirCallableDeclaration.kirCallableDeclarationOrNull: KirCallableDeclaration<*>?
        get() = kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)
}
