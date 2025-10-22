package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirIrFile

object AddFoundationImportsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.skieModuleFiles
            .filterIsInstance<SirIrFile>()
            .forEach {
                it.imports.add("Foundation")
            }
    }
}
