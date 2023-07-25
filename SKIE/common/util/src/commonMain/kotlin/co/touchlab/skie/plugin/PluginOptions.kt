package co.touchlab.skie.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import java.io.File

object SkiePlugin {

    const val id = "co.touchlab.skie"

    object Options {

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
