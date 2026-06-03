package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase

object FlowBridgingConfigurationPhase : SirPhase {

    context(context: SirPhase.Context)
    override fun isActive(): Boolean = context.run { SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled }

    context(context: SirPhase.Context)
    override suspend fun execute() {
        SupportedFlow.values().forEach {
            configureFlowBridging(it)
        }
    }

    context(context: SirPhase.Context)
    private fun configureFlowBridging(supportedFlow: SupportedFlow) {
        supportedFlow.variants.forEach {
            it.getKotlinKirClass().bridgedSirClass = it.getSwiftClass()
        }
    }
}
