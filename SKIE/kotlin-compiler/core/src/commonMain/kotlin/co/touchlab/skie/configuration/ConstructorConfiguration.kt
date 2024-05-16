package co.touchlab.skie.configuration

class ConstructorConfiguration(
    parent: FileOrClassConfiguration,
) : FunctionConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Constructor =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Constructor =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Constructor {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Constructor {
        resetUnsafe(configurationKey)
    }
}
