package co.touchlab.skie.configuration.provider

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.UntypedSkieConfigurationData
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CompilerSkieConfigurationData(
    override val enabledConfigurationFlags: Set<SkieConfigurationFlag> = emptySet(),
    val disabledConfigurationFlags: Set<SkieConfigurationFlag> = emptySet(),
    override val groups: List<Group> = emptyList(),
) : UntypedSkieConfigurationData<SkieConfigurationFlag> {

    init {
        require(enabledConfigurationFlags.intersect(disabledConfigurationFlags).isEmpty()) {
            "A configuration flag cannot be both enabled and disabled. Problem with: ${
                enabledConfigurationFlags.intersect(
                    disabledConfigurationFlags,
                )
            }"
        }
    }

    fun serialize(): String {
        val json = Json { prettyPrint = true }

        return json.encodeToString(this)
    }

    operator fun plus(other: CompilerSkieConfigurationData): CompilerSkieConfigurationData =
        CompilerSkieConfigurationData(
            (enabledConfigurationFlags - other.disabledConfigurationFlags) + other.enabledConfigurationFlags,
            other.disabledConfigurationFlags,
            groups + other.groups,
        )

    @Serializable
    data class Group(
        override val target: String,
        override val overridesAnnotations: Boolean,
        override val items: Map<String, String?>,
    ) : UntypedSkieConfigurationData.Group

    companion object {

        fun deserialize(string: String): CompilerSkieConfigurationData =
            Json.decodeFromString(string)
    }
}
