package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively

object AddFoundationImportsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.files.forEach {
            it.imports.add("Foundation")
        }
    }
}
