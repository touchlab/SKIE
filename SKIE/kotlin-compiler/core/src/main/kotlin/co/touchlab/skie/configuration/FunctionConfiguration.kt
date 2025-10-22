package co.touchlab.skie.configuration

abstract class FunctionConfiguration(
    parent: FileOrClassConfiguration,
) : CallableDeclarationConfiguration(parent) {

    @JvmName("hasFunction")
    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Function =
        hasUnsafe(configurationKey)

    @JvmName("getFunction")
    operator fun <KEY, VALUE> get(configurationKey: KEY): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Function =
        getUnsafe(configurationKey)

    @JvmName("setFunction")
    operator fun <KEY, VALUE> set(configurationKey: KEY, value: VALUE) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Function {
        setUnsafe(configurationKey, value)
    }

    @JvmName("resetFunction")
    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.Function {
        resetUnsafe(configurationKey)
    }
}
