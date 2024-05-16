package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.common.CommonSkieConfiguration

class ClassConfiguration(
    parent: FileOrClassConfiguration,
) : SkieConfiguration(parent.configuration) {

    var flowMappingStrategy by value {
        CommonSkieConfiguration.getDefaultFlowMappingStrategy(this)
    }

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Class =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Class =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Class {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Class {
        resetUnsafe(configurationKey)
    }
}
