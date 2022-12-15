package co.touchlab.skie.plugin

import java.io.File

object SkiePlugin {

    const val id = "co.touchlab.skie"

    object Options {

        val swiftSourceFile = PluginOption(
            optionName = "swiftSourceFile",
            valueDescription = "<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val generatedSwiftDir = PluginOption(
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
