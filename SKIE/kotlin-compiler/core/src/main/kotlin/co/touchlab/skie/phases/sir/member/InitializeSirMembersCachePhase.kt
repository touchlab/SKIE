package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.phases.SirPhase

object InitializeSirMembersCachePhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.kirProvider.initializeSirCallableDeclarationsCache()
    }
}
