package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase

object ConfigureStableNameTypeAliasesForKotlinRuntimePhase : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(SirPhase.Context)
    override suspend fun execute() {
        SupportedFlow.values().forEach {
            it.getCoroutinesKirClass().enableStableNameTypeAlias()
        }

        kirProvider.getClassByFqName("kotlinx.coroutines.Runnable").enableStableNameTypeAlias()
    }

    private fun KirClass.enableStableNameTypeAlias() {
        configuration[ClassInterop.StableTypeAlias] = true
    }
}
