package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.common.CommonSkieConfiguration

abstract class CallableDeclarationConfiguration(
    parent: FileOrClassConfiguration,
) : SkieConfiguration(parent.configuration) {

    var flowMappingStrategy by value {
        CommonSkieConfiguration.getDefaultFlowMappingStrategy(this)
    }

    @JvmName("hasCallableDeclaration")
    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.CallableDeclaration =
        hasUnsafe(configurationKey)

    @JvmName("getCallableDeclaration")
    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.CallableDeclaration =
        getUnsafe(configurationKey)

    @JvmName("setCallableDeclaration")
    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.CallableDeclaration {
        setUnsafe(configurationKey, value)
    }

    @JvmName("resetCallableDeclaration")
    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.CallableDeclaration {
        resetUnsafe(configurationKey)
    }
}
