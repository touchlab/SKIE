package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.util.plugin.SkiePlugin
import org.jetbrains.kotlin.compiler.plugin.CliOption

fun SkiePlugin.Option<*>.toCliOption() = CliOption(
    optionName,
    valueDescription,
    description,
    isRequired,
    allowMultipleOccurrences,
)
