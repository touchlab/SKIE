package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.overrideConfiguration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.classDescriptorOrError
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.features.flow.SupportedFlow

object ConfigureStableNameTypeAliasesForKotlinRuntimePhase : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        SupportedFlow.values().forEach {
            it.getCoroutinesKirClass().enableStableNameTypeAlias()
        }

        kirProvider.getClassByFqName("kotlinx.coroutines.Runnable").enableStableNameTypeAlias()
    }

    context(SirPhase.Context)
    private fun KirClass.enableStableNameTypeAlias() {
        classDescriptorOrError.overrideConfiguration(ClassInterop.StableTypeAliases, true)
    }
}
