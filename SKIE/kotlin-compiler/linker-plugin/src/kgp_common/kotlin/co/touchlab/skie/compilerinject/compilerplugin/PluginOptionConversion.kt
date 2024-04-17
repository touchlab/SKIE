package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.util.plugin.PluginOption
import org.jetbrains.kotlin.compiler.plugin.CliOption

fun PluginOption<*>.toCliOption() = CliOption(
    optionName,
    valueDescription,
    description,
    isRequired,
    allowMultipleOccurrences,
)
