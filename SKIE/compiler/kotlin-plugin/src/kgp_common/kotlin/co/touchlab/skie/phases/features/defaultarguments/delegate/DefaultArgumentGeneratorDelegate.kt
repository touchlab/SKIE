package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.phases.DescriptorModificationPhase

interface DefaultArgumentGeneratorDelegate {

    context(DescriptorModificationPhase.Context)
    fun generate()
}
