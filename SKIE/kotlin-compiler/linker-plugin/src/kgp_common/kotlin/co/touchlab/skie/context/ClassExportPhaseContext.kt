package co.touchlab.skie.context

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.ClassExportPhase

class ClassExportPhaseContext(
    mainSkieContext: MainSkieContext,
) : ClassExportPhase.Context, ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: ClassExportPhaseContext = this

    val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}
