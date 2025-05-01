package co.touchlab.skie.configuration

class PackageConfiguration(parent: ModuleConfiguration) : SkieConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Package =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Package =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(
        configurationKey: KEY,
        value: VALUE,
    ) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Package {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Package {
        resetUnsafe(configurationKey)
    }
}
