package co.touchlab.skie.util.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import java.io.File

object SkiePlugin {

    const val id = "co.touchlab.skie"

    object Options {

        val skieDirectories = Option(
            optionName = "skieBuildDirectory",
            valueDescription = "<absolute path>",
            description = "",
            isRequired = true,
            serialize = { it.buildDirectory.directory.absolutePath },
            deserialize = { SkieDirectories(File(it)) },
        )
    }

    data class Option<T>(
        val optionName: String,
        val valueDescription: String,
        val description: String,
        val isRequired: Boolean = false,
        val allowMultipleOccurrences: Boolean = false,
        val serialize: (T) -> String,
        val deserialize: (String) -> T,
    )
}
