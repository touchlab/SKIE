package co.touchlab.skie.configuration

import co.touchlab.skie.context.CommonSkieContext

class SimpleFunctionConfiguration(val parent: FileOrClassConfiguration) : FunctionConfiguration(parent) {

    fun <KEY, VALUE> has(configurationKey: KEY): Boolean where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.SimpleFunction =
        hasUnsafe(configurationKey)

    operator fun <KEY, VALUE> get(
        configurationKey: KEY,
    ): VALUE where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.SimpleFunction = getUnsafe(configurationKey)

    operator fun <KEY, VALUE> set(
        configurationKey: KEY,
        value: VALUE,
    ) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.SimpleFunction {
        setUnsafe(configurationKey, value)
    }

    fun <KEY, VALUE> reset(configurationKey: KEY) where KEY : ConfigurationKey<VALUE>, KEY : ConfigurationScope.SimpleFunction {
        resetUnsafe(configurationKey)
    }
}

context(CommonSkieContext)
val SimpleFunctionConfiguration.isSuspendInteropEnabled: Boolean
    get() = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled && this[SuspendInterop.Enabled]
