package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isAbstract

object ConfigureInternalVisibilityForWrappedCallableDeclarationsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalCallableDeclarations
            .filter { it.isWrappedBySkie && it.shouldBeInternalIfWrappedBySkie && it.visibility == SirVisibility.Public && !it.isAbstract }
            .forEach {
                it.visibility = SirVisibility.Internal
            }
    }

    context(SirPhase.Context)
    private val SirCallableDeclaration.shouldBeInternalIfWrappedBySkie: Boolean
        // TODO Once SirCallableDeclaration origin is added check even for intermediate wrappers and configure those based on the Kir configuration as well.
        get() = kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)?.configuration?.get(SkieVisibility) ==
            SkieVisibility.Level.InternalIfWrapped
}
