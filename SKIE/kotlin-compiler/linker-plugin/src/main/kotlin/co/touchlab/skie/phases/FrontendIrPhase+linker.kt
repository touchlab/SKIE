package co.touchlab.skie.phases

import co.touchlab.skie.context.FrontendIrPhaseContext
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

val FrontendIrPhase.Context.declarationBuilder: DeclarationBuilder
    get() = typedContext.declarationBuilder

private val FrontendIrPhase.Context.typedContext: FrontendIrPhaseContext
    get() = context as FrontendIrPhaseContext
