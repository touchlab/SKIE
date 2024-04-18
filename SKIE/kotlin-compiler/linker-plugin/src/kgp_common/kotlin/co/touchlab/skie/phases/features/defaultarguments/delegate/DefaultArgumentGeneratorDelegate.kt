package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.phases.FrontendIrPhase

interface DefaultArgumentGeneratorDelegate {

    context(FrontendIrPhase.Context)
    fun generate()
}
