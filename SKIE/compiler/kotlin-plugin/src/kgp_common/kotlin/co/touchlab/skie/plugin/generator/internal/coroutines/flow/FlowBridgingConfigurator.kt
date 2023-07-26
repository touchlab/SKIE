package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class FlowBridgingConfigurator(
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean = SkieFeature.CoroutinesInterop in skieContext.skieConfiguration.enabledFeatures

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
