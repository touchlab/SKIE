package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object AddFoundationImportsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.files
            .filter { it.content.isEmpty() }
            .forEach {
                it.imports.add("Foundation")
            }
    }
}
