package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

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
            it.kotlinFlowModel.bridge = ObjcSwiftBridge.FromSKIE(it.swiftFlowDeclaration)
        }
    }
}
