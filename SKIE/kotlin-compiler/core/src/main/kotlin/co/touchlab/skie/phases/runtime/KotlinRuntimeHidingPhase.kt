package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.phases.SirPhase

object KotlinRuntimeHidingPhase : SirPhase {

    context(context: SirPhase.Context)
    override fun isActive(): Boolean = context.run { SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled }

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.kirProvider.kotlinClasses
            .filter { it.module.origin == KirModule.Origin.SkieRuntime }
            .forEach {
                it.originalSirClass.isHidden = true
            }
    }
}
