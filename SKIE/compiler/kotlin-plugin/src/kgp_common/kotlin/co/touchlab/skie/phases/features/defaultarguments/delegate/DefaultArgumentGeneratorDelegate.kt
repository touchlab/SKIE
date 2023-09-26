package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.phases.SkiePhase

interface DefaultArgumentGeneratorDelegate {

    context(SkiePhase.Context)
    fun generate()
}
