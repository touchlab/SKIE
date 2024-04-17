package co.touchlab.skie.phases.kir

import co.touchlab.skie.phases.KirCorePhase
import co.touchlab.skie.phases.KirPhase

object InitializeKirMembersCachePhase : KirCorePhase {

    context(KirPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeCallableDeclarationsCache()
    }
}
