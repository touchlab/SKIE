package co.touchlab.skie.configuration

class PropertyConfiguration(
    val parent: FileOrClassConfiguration,
) : CallableDeclarationConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Property =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Property =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Property {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Property {
        resetUnsafe(configurationKey)
    }
}
