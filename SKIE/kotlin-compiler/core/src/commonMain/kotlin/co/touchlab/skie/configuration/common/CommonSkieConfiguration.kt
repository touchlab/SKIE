package co.touchlab.skie.configuration.common

import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag

object CommonSkieConfiguration {

    fun getDefaultFlowMappingStrategy(configuration: SkieConfiguration): FlowMappingStrategy {
        val isFlowInteropEnabled = configuration.globalConfiguration.isFlagEnabled(SkieConfigurationFlag.Feature_CoroutinesInterop) &&
            configuration.getUnsafe(FlowInterop.Enabled)

        return if (isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None
    }
}
