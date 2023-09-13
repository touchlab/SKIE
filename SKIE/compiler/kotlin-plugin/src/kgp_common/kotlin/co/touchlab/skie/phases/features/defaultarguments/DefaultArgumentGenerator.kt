package co.touchlab.skie.phases.features.defaultarguments

import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.features.defaultarguments.delegate.ClassMethodsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ConstructorsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ExtensionFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.TopLevelFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.util.SharedCounter

class DefaultArgumentGenerator(
    private val context: DescriptorModificationPhase.Context,
) : DescriptorModificationPhase {

    private val sharedCounter = SharedCounter()

    private val delegates = listOf(
        ::ClassMethodsDefaultArgumentGeneratorDelegate,
        ::ConstructorsDefaultArgumentGeneratorDelegate,
        ::TopLevelFunctionDefaultArgumentGeneratorDelegate,
        ::ExtensionFunctionDefaultArgumentGeneratorDelegate,
    ).map { it(context, sharedCounter) }

    context(DescriptorModificationPhase.Context)
    override fun execute() {
        delegates.forEach {
            it.generate()
        }
    }

    object FinalizePhase : StatefulSirPhase()
}
