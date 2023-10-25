package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.phases.SirPhase

object InitializeSirTypesCachePhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.initializeSirClassCache()
    }
}
