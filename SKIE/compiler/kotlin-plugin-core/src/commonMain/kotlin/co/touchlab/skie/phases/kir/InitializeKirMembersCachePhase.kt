package co.touchlab.skie.phases.kir

import co.touchlab.skie.phases.CompilerIndependentKirPhase
import co.touchlab.skie.phases.KirPhase

object InitializeKirMembersCachePhase : CompilerIndependentKirPhase {

    context(KirPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeCallableDeclarationsCache()
    }
}
