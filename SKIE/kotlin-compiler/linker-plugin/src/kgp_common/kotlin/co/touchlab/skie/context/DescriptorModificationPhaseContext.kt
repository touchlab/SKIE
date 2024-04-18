package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.DescriptorModificationPhase

class DescriptorModificationPhaseContext(
    mainSkieContext: MainSkieContext,
) : DescriptorModificationPhase.Context, ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: DescriptorModificationPhaseContext = this

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
