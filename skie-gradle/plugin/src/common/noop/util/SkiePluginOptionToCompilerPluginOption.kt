package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.PluginOption
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal fun <T> PluginOption<T>.subpluginOption(value: T): SubpluginOption = SubpluginOption(optionName, serialize(value))
