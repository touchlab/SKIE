package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.util.isBaseDeclaration
import co.touchlab.skie.phases.SirPhase

object PropagateSirIsHiddenPropertyInMembersPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinOverridableDeclaration
            .filter { it.isBaseDeclaration && it.overriddenBy.isNotEmpty() }
            .forEach {
                it.propagateIsHidden()
            }
    }

    private fun KirOverridableDeclaration<*, *>.propagateIsHidden() {
        overriddenBy.forEach { overriddenByDeclaration ->
            if (!overriddenByDeclaration.configuration.has(SkieVisibility) && this.originalSirCallableDeclaration.isHidden) {
                overriddenByDeclaration.originalSirCallableDeclaration.isHidden = true
            }

            overriddenByDeclaration.propagateIsHidden()
        }
    }
}
