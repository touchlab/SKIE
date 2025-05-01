package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.common.CommonSkieConfiguration

class ValueParameterConfiguration(parent: ValueParameterConfigurationParent) : SkieConfiguration(parent.configuration) {

    constructor(parent: CallableDeclarationConfiguration) : this(ValueParameterConfigurationParent.CallableDeclaration(parent))

    var flowMappingStrategy by value {
        CommonSkieConfiguration.getDefaultFlowMappingStrategy(parent.configuration)

        // TODO Use this instead after Flow annotations allow ValueParameters and the configuration is hierarchical
//         CommonSkieConfiguration.getDefaultFlowMappingStrategy(this)
    }

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(
        configurationKey: KEY,
    ): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter = getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(
        configurationKey: KEY,
        value: VALUE,
    ) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.ValueParameter {
        resetUnsafe(configurationKey)
    }
}
