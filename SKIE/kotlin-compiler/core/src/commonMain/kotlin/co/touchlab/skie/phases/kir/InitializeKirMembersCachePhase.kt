package co.touchlab.skie.phases.kir

import co.touchlab.skie.phases.KirPhase

object InitializeKirMembersCachePhase : KirPhase {

    context(KirPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeCallableDeclarationsCache()
    }
}
