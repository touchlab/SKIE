package co.touchlab.swiftlink.plugin

import java.io.File

object SkiePlugin {
    const val id = "co.touchlab.skie"

    object Options {
        val linkPhaseSwiftPackOutputDir = PluginOption(
            optionName = "linkPhaseSwiftPackOutputDir",
            valueDescription = "<absolute path>",
            description = "",
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val swiftSourceFile = PluginOption(
            optionName = "swiftSourceFile",
            valueDescription = "<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val expandedSwiftDir = PluginOption(
            optionName = "expandedSwiftDir",
            valueDescription = "<absolute path>",
            description = "",
            isRequired = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val disableWildcardExport = PluginOption(
            optionName = "disableWildcardExport",
            valueDescription = "<true|false>",
            description = "",
            serialize = Boolean::toString,
            deserialize = String::toBooleanStrict,
        )

        val swiftGenConfigPath = PluginOption(
            optionName = "swiftGenConfigPath",
            valueDescription = "<absolute path>",
            description = "Path to JSON file with SwiftGen configuration.",
            isRequired = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )
    }
}
