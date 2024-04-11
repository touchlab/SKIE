package co.touchlab.skie.configuration

import co.touchlab.skie.phases.features.flow.FlowMappingStrategy

class ValueParameterConfiguration(
    parent: ValueParameterConfigurationParent,
) : SkieConfiguration(parent.configuration) {

    constructor(parent: CallableDeclarationConfiguration) : this(ValueParameterConfigurationParent.CallableDeclaration(parent))

    var flowMappingStrategy by value {
        val isFlowInteropEnabled = parent.configuration.getUnsafe(FlowInterop.Enabled)

        if (isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None

        // TODO Use this instead after Flow configuration is hierarchical
//         CommonSkieConfiguration.getDefaultFlowMappingStrategy(this)
    }

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter {
        resetUnsafe(configurationKey)
    }
}
