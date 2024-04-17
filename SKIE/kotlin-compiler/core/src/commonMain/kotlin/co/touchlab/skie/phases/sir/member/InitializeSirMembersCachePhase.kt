package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.phases.SirPhase

object InitializeSirMembersCachePhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.initializeSirCallableDeclarationsCache()
    }
}
