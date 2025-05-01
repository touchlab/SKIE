package co.touchlab.skie.configuration

class GlobalConfiguration(enabledFlags: Set<SkieConfigurationFlag>, private val supportedKeys: Set<ConfigurationKey<*>>) :
    SkieConfiguration(null) {

    private val mutableEnabledFlags = enabledFlags.toMutableSet()

    val enabledFlags: Set<SkieConfigurationFlag> by ::mutableEnabledFlags

    override val globalConfiguration: GlobalConfiguration
        get() = this

    fun isFlagEnabled(flag: SkieConfigurationFlag): Boolean = flag in enabledFlags

    fun disableFlag(flag: SkieConfigurationFlag) {
        mutableEnabledFlags.remove(flag)
    }

    fun enableFlag(flag: SkieConfigurationFlag) {
        mutableEnabledFlags.add(flag)
    }

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Global =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Global =
        getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(
        configurationKey: KEY,
        value: VALUE,
    ) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Global {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Global {
        resetUnsafe(configurationKey)
    }

    override fun isKeySupported(key: ConfigurationKey<*>): Boolean = key in supportedKeys
}
