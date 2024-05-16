package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirOverridableDeclaration

object PropagateSirVisibilityToAbstractMembersPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalCallableDeclarations
            .filterIsInstance<SirOverridableDeclaration<*>>()
            // WIP Refactor once Sir also has modality
            .filter { it.memberOwner?.kind == SirClass.Kind.Protocol || (it.memberOwner?.kind == SirClass.Kind.Class && it.kirCallableDeclarationOrNull?.modality == KirCallableDeclaration.Modality.Abstract) }
            .forEach {
                updateVisibility(it)
            }
    }

    context(SirPhase.Context)
    private fun updateVisibility(sirOverridableDeclaration: SirOverridableDeclaration<*>) {
        val ownerVisibility = sirOverridableDeclaration.memberOwner?.visibility ?: return

        if (sirOverridableDeclaration.visibility == ownerVisibility) {
            return
        }

        if (sirOverridableDeclaration.hasExplicitVisibility) {
            kirReporter.warning(
                "Abstract member must have the same SkieVisibility as parent. The visibility of the member will be set to match the visibility of the parent. " +
                        "This warning might become an error in the future.",
                sirOverridableDeclaration.kirCallableDeclarationOrNull,
            )
        }

        sirOverridableDeclaration.visibility = ownerVisibility
    }

    context(SirPhase.Context)
    private val SirOverridableDeclaration<*>.hasExplicitVisibility: Boolean
        get() = this.kirCallableDeclarationOrNull?.configuration?.has(SkieVisibility) == true

    context(SirPhase.Context)
    private val SirCallableDeclaration.kirCallableDeclarationOrNull: KirCallableDeclaration<*>?
        get() = kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)
}
