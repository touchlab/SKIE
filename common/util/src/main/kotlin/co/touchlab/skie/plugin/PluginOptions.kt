package co.touchlab.skie.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import java.io.File

object SkiePlugin {

    const val id = "co.touchlab.skie"

    object Options {

        val buildId = PluginOption(
            optionName = "buildId",
            valueDescription = "string",
            description = "SKIE build ID",
            isRequired = true,
            serialize = { it },
            deserialize = { it },
        )

        val skieDirectories = PluginOption(
            optionName = "skieBuildDirectory",
            valueDescription = "<absolute path>",
            description = "",
            isRequired = true,
            serialize = { it.buildDirectory.directory.absolutePath },
            deserialize = { SkieDirectories(File(it)) },
        )
    }
}
