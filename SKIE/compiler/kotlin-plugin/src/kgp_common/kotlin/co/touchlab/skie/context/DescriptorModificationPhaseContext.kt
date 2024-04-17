package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.CompilerDependentForegroundPhase
import co.touchlab.skie.phases.DescriptorModificationPhase

class DescriptorModificationPhaseContext(
    mainSkieContext: MainSkieContext,
) : DescriptorModificationPhase.Context, CompilerDependentForegroundPhase.Context by mainSkieContext {

    override val context: DescriptorModificationPhaseContext = this

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
