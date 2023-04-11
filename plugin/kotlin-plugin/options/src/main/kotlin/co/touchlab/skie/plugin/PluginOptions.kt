package co.touchlab.skie.plugin

import java.io.File
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint

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

        val skieConfigurationPath = PluginOption(
            optionName = "skieConfigurationPath",
            valueDescription = "<absolute path>",
            description = "Path to JSON file with SKIE configuration.",
            isRequired = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val buildId = PluginOption(
            optionName = "buildId",
            valueDescription = "string",
            description = "SKIE build ID",
            isRequired = true,
            serialize = { it },
            deserialize = { it },
        )

        val jwtWithLicense = PluginOption(
            optionName = "jwtWithLicense",
            valueDescription = "JWT",
            description = "JWT with SKIE license",
            isRequired = true,
            serialize = { it },
            deserialize = { it },
        )

        val analyticsDir = PluginOption(
            optionName = "analyticsDir",
            valueDescription = "<absolute path>",
            description = "Path to directory with SKIE analytics.",
            isRequired = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        object Debug {
            val infoDirectory = PluginOption(
                optionName = "debugInfoDirectory",
                valueDescription = "<absolute path>",
                description = "Path where logs and other debug outputs will be stored",
                isRequired = true,
                serialize = File::getAbsolutePath,
                deserialize = ::File,
            )

            val dumpSwiftApiAt = PluginOption(
                optionName = "debugDumpSwiftApiAt",
                valueDescription = "BeforeApiNotes|AfterApiNotes",
                description = "Where to dump Swift API",
                isRequired = false,
                allowMultipleOccurrences = true,
                serialize = { it.name },
                deserialize = { value ->
                    DumpSwiftApiPoint.values().find { it.name.equals(value, ignoreCase = true) } ?: error("Invalid Swift API dump point ${value}")
                }
            )
        }

    }
}
