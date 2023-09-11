package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.phases.SkieCompilationPhase

internal class FlowBridgingConfigurator(
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieContext.skieConfiguration.enabledConfigurationFlags

    override fun runObjcPhase() {
        skieContext.module.configure {
            SupportedFlow.values().forEach {
                configureFlowBridging(it)
            }
        }
    }

    private fun MutableSwiftModelScope.configureFlowBridging(supportedFlow: SupportedFlow) {
        supportedFlow.variants.forEach {
            it.kotlinFlowModel.bridgedSirClass = it.swiftFlowClass()
        }
    }
}
