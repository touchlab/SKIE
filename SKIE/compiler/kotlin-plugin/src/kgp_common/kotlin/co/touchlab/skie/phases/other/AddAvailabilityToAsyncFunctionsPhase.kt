package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively

object AddAvailabilityToAsyncFunctionsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.skieModule.getAllDeclarationsRecursively()
            .filterIsInstance<SirSimpleFunction>()
            .filter { it.isAsync }
            .forEach {
                it.attributes.add("available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)")
            }
    }
}
