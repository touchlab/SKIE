package co.touchlab.skie.phases.runtime

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirVisibility

object KotlinRuntimeHidingPhase : SirPhase {

    // WIP 2 Should the whole runtime be hidden?
    context(SirPhase.Context)
    override fun execute() {
        descriptorProvider.exposedClasses
            .filter { it.belongsToSkieRuntime }
            .forEach {
                it.swiftModel.kotlinSirClass.visibility = SirVisibility.PublicButHidden
            }
    }
}
