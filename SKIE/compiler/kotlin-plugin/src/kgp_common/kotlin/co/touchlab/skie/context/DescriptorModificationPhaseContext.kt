package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SkiePhase

class DescriptorModificationPhaseContext(
    mainSkieContext: MainSkieContext,
) : DescriptorModificationPhase.Context, SkiePhase.Context by mainSkieContext {

    override val context: DescriptorModificationPhaseContext = this

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
