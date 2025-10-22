package co.touchlab.skie.configuration

class FileConfiguration(
    parent: PackageConfiguration,
) : SkieConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.File =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.File =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.File {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.File {
        resetUnsafe(configurationKey)
    }
}
