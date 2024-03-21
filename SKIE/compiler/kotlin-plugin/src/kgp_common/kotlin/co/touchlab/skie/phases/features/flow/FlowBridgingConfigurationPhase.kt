package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase

object FlowBridgingConfigurationPhase : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override suspend fun execute() {
        SupportedFlow.values().forEach {
            configureFlowBridging(it)
        }
    }

    context(SirPhase.Context)
    private fun configureFlowBridging(supportedFlow: SupportedFlow) {
        supportedFlow.variants.forEach {
            it.getKotlinKirClass().bridgedSirClass = it.getSwiftClass()
        }
    }
}
