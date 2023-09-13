package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope

object FlowBridgingConfigurator : SirPhase {

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        SupportedFlow.values().forEach {
            configureFlowBridging(it)
        }
    }

    private fun MutableSwiftModelScope.configureFlowBridging(supportedFlow: SupportedFlow) {
        supportedFlow.variants.forEach {
            it.kotlinFlowModel.bridgedSirClass = it.swiftFlowClass()
        }
    }
}
