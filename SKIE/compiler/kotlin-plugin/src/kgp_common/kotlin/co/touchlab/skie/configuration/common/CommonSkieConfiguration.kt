package co.touchlab.skie.configuration.common

import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.features.flow.FlowMappingStrategy

object CommonSkieConfiguration {

    fun getDefaultFlowMappingStrategy(configuration: SkieConfiguration): FlowMappingStrategy {
        val isFlowInteropEnabled = configuration.getUnsafe(FlowInterop.Enabled)

        return if (isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None
    }
}
