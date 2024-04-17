package co.touchlab.skie.configuration

class RootConfiguration(
    val enabledFlags: Set<SkieConfigurationFlag>,
    private val supportedKeys: Set<ConfigurationKey<*>>,
) : SkieConfiguration(null) {

    override val rootConfiguration: RootConfiguration
        get() = this

    fun isFlagEnabled(flag: SkieConfigurationFlag): Boolean =
        flag in enabledFlags

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Root =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Root {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Root {
        resetUnsafe(configurationKey)
    }

    override fun isKeySupported(key: ConfigurationKey<*>): Boolean =
        key in supportedKeys
}
