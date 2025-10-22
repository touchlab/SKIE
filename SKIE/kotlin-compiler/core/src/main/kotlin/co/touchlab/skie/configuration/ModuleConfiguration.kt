package co.touchlab.skie.configuration

class ModuleConfiguration(
    parent: GlobalConfiguration,
) : SkieConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Module =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Module =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Module {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Module {
        resetUnsafe(configurationKey)
    }
}
