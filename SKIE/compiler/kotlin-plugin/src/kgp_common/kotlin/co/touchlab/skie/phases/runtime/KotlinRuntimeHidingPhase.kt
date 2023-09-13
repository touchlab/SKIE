package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SirPhase

object KotlinRuntimeHidingPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        descriptorProvider.exposedClasses
            .filter { it.belongsToSkieRuntime }
            .forEach {
                it.swiftModel.visibility = co.touchlab.skie.swiftmodel.SwiftModelVisibility.Hidden
            }
    }
}
