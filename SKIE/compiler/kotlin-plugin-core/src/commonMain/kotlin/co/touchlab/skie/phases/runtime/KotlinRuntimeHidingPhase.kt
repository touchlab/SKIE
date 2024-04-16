package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirVisibility

object KotlinRuntimeHidingPhase : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses
            .filter { it.module.origin == KirModule.Origin.SkieRuntime }
            .forEach {
                it.originalSirClass.visibility = SirVisibility.PublicButHidden
            }
    }
}
