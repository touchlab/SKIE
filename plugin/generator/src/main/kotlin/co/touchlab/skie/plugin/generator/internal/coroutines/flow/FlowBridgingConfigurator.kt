package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.SwiftTypeSwiftModel
import co.touchlab.skie.plugin.api.util.flow.SupportedFlow
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class FlowBridgingConfigurator(
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        skieContext.module.configure {
            SupportedFlow.values().forEach {
                configureFlowBridging(it)
            }
        }
    }

    private fun MutableSwiftModelScope.configureFlowBridging(supportedFlow: SupportedFlow) {
        configureFlowBridging(supportedFlow.toNonOptionalFqName)
        configureFlowBridging(supportedFlow.toOptionalFqName)
    }

    private fun MutableSwiftModelScope.configureFlowBridging(toFqName: String) {
        val flowModel = referenceClass(toFqName)

        val bridgeIdentifier = toFqName.substringAfterLast(".").replace("Kotlin", "Swift")

        flowModel.bridge = SwiftTypeSwiftModel(null, bridgeIdentifier, false)
    }
}
