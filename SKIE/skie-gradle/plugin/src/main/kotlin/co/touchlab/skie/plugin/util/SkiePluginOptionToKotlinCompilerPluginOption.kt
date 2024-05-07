package co.touchlab.skie.plugin.util

import co.touchlab.skie.util.plugin.SkiePlugin

internal fun <T> SkiePlugin.Option<T>.toKotlinCompilerPluginOption(value: T): KotlinCompilerPluginOption =
    KotlinCompilerPluginOption(optionName, serialize(value))
