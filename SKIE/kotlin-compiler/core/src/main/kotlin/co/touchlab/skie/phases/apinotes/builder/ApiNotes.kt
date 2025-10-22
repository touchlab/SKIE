package co.touchlab.skie.phases.apinotes.builder

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiNotes(
    @SerialName("Name")
    val moduleName: String,
    @SerialName("Classes")
    val classes: List<ApiNotesType> = emptyList(),
    @SerialName("Protocols")
    val protocols: List<ApiNotesType> = emptyList(),
) {

    fun createApiNotesFileContent(): String = coder.encodeToString(serializer(), this)

    companion object {

        private val coder = Yaml(
            configuration = YamlConfiguration(
                encodeDefaults = false,
                strictMode = false,
                breakScalarsAt = Int.MAX_VALUE,
            ),
        )
    }
}
