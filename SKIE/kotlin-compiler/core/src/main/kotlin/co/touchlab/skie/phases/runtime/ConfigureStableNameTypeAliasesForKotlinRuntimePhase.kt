package co.touchlab.skie.phases.runtime

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase

object ConfigureStableNameTypeAliasesForKotlinRuntimePhase : SirPhase {

    context(context: SirPhase.Context)
    override fun isActive(): Boolean = with(context) { SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled }

    context(context: SirPhase.Context)
    override suspend fun execute() {
        SupportedFlow.values().forEach {
            it.getCoroutinesKirClass().enableStableNameTypeAlias()
        }

        context.kirProvider.getClassByFqName("kotlinx.coroutines.Runnable").enableStableNameTypeAlias()
    }

    private fun KirClass.enableStableNameTypeAlias() {
        configuration[ClassInterop.StableTypeAlias] = true
    }
}
