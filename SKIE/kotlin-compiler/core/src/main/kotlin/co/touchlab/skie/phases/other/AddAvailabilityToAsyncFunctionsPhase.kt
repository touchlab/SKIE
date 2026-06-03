package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object AddAvailabilityToAsyncFunctionsPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.sirProvider.allSkieGeneratedSimpleFunctions
            .filter { it.isAsync }
            .forEach {
                it.attributes.add("available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)")
            }
    }
}
