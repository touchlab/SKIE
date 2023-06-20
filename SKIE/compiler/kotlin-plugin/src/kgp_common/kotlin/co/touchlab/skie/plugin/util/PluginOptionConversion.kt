package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.PluginOption
import org.jetbrains.kotlin.compiler.plugin.CliOption

internal fun PluginOption<*>.toCliOption() = CliOption(
    optionName,
    valueDescription,
    description,
    isRequired,
    allowMultipleOccurrences,
)
