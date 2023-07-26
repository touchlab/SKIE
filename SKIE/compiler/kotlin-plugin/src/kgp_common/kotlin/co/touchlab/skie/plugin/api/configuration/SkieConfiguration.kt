package co.touchlab.skie.plugin.api.configuration

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.TypedSkieConfiguration
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.SkieFeature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class SkieConfiguration(
    override val enabledFeatures: Set<SkieFeature> = emptySet(),
    val disabledFeatures: Set<SkieFeature> = emptySet(),
    override val groups: List<Group> = emptyList(),
) : TypedSkieConfiguration<SkieFeature> {

    init {
        require(enabledFeatures.intersect(disabledFeatures).isEmpty()) {
            "A feature cannot be both enabled and disabled. Problem with: ${enabledFeatures.intersect(disabledFeatures)}"
        }
    }

    operator fun <T> get(target: ConfigurationTarget, key: ConfigurationKey<T>): T {
        if (target.belongsToSkieRuntime) {
            return key.skieRuntimeValue
        }

        val group = findGroup(target, key)

        val configurationValue = group?.findValue(key) ?: key.defaultValue

        return if (group?.overridesAnnotations == true) {
            configurationValue
        } else {
            key.getAnnotationValue(target) ?: configurationValue
        }
    }

    private fun findGroup(target: ConfigurationTarget, key: ConfigurationKey<*>): Group? =
        groups.lastOrNull { target.fqName.startsWith(it.target) && key.name in it.items }

    private fun <T> Group.findValue(key: ConfigurationKey<T>): T? =
        this.items[key.name]?.let { key.deserialize(it) }

    fun serialize(): String {
        val json = Json { prettyPrint = true }

        return json.encodeToString(this)
    }

    operator fun plus(other: SkieConfiguration): SkieConfiguration =
        SkieConfiguration(
            (enabledFeatures - other.disabledFeatures) + other.enabledFeatures,
            other.disabledFeatures,
            groups + other.groups,
        )

    @Serializable
    data class Group(
        override val target: String,
        override val overridesAnnotations: Boolean,
        override val items: Map<String, String?>,
    ) : TypedSkieConfiguration.Group

    companion object {

        fun deserialize(string: String): SkieConfiguration =
            Json.decodeFromString(string)
    }
}
