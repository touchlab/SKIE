package co.touchlab.skie.context

import co.touchlab.skie.phases.FinalizePhase
import co.touchlab.skie.phases.SkiePhase

class FinalizePhaseContext(
    mainSkieContext: MainSkieContext,
) : FinalizePhase.Context, SkiePhase.Context by mainSkieContext {

    override val context: FinalizePhase.Context = this
}
