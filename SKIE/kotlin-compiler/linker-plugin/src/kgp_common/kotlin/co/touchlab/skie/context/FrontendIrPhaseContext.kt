package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.FrontendIrPhase

class FrontendIrPhaseContext(mainSkieContext: MainSkieContext) :
    FrontendIrPhase.Context,
    ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: FrontendIrPhaseContext = this

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
