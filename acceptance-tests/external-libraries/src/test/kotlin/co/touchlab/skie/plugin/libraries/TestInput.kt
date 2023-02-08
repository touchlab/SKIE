package co.touchlab.skie.plugin.libraries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TestInput(
    val files: List<String>,
    @SerialName("exported-files")
    val exportedFiles: List<String>,
)
