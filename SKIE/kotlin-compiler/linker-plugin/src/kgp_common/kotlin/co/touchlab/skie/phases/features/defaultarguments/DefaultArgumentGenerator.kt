package co.touchlab.skie.phases.features.defaultarguments

import co.touchlab.skie.phases.FrontendIrPhase
import co.touchlab.skie.phases.features.defaultarguments.delegate.ClassMethodsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ConstructorsDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.ExtensionFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.features.defaultarguments.delegate.TopLevelFunctionDefaultArgumentGeneratorDelegate
import co.touchlab.skie.phases.util.StatefulCompilerDependentKirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.util.SharedCounter

class DefaultArgumentGenerator(
    private val context: FrontendIrPhase.Context,
) : FrontendIrPhase {

    private val sharedCounter = SharedCounter()

    private val delegates = listOf(
        ::ClassMethodsDefaultArgumentGeneratorDelegate,
        ::ConstructorsDefaultArgumentGeneratorDelegate,
        ::TopLevelFunctionDefaultArgumentGeneratorDelegate,
        ::ExtensionFunctionDefaultArgumentGeneratorDelegate,
    ).map { it(context, sharedCounter) }

    context(FrontendIrPhase.Context)
    override suspend fun execute() {
        delegates.forEach {
            it.generate()
        }
    }

    object RegisterOverloadsPhase : StatefulCompilerDependentKirPhase()

    object RemoveManglingOfOverloadsInitPhase : StatefulCompilerDependentKirPhase()

    object RemoveManglingOfOverloadsFinalizePhase : StatefulSirPhase()
}
