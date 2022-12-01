package co.touchlab.skie.configuration

import co.touchlab.skie.configuration_api.BuildConfig
import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import co.touchlab.skie.configuration.features.SkieFeatureSet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class Configuration(
    val enabledFeatures: SkieFeatureSet,
    private val groups: List<Group>,
) {

    operator fun <T> get(target: ConfigurationTarget, key: ConfigurationKey<T>): T {
        val group = findGroup(target, key)

        val configurationValue = group?.findValue(key) ?: key.defaultValue

        return if (group?.overridesAnnotations == true) {
            configurationValue
        } else {
            key.getAnnotationValue(target) ?: configurationValue
        }
    }

    private fun findGroup(target: ConfigurationTarget, key: ConfigurationKey<*>): Group? =
        groups.lastOrNull { target.fqName.startsWith(it.targetFqNamePrefix) && key.name in it.items }

    private fun <T> Group.findValue(key: ConfigurationKey<T>): T? =
        this.items[key.name]?.let { key.deserialize(it) }

    fun serialize(): String {
        val json = Json { prettyPrint = true }

        val wrapper = SerializationWrapper(enabledFeatures, groups)

        return json.encodeToString(wrapper)
    }

    operator fun plus(other: Configuration): Configuration =
        Configuration(enabledFeatures + other.enabledFeatures, groups + other.groups)

    @Serializable
    data class SerializationWrapper(
        val enabledFeatures: SkieFeatureSet,
        val groups: List<Group>,
    )

    @Serializable
    data class Group(
        @SerialName("target")
        val targetFqNamePrefix: String,
        val overridesAnnotations: Boolean,
        val items: Map<String, String?>,
    )

    companion object {

        const val CliPluginId: String = BuildConfig.PLUGIN_ID
        const val CliOptionKey: String = "config"

        operator fun invoke(builder: ConfigurationBuilder.() -> Unit): Configuration =
            ConfigurationBuilder().also(builder).build()

        operator fun invoke(builder: ConfigurationBuilder): Configuration =
            builder.build()

        fun deserialize(string: String): Configuration {
            val wrapper = Json.decodeFromString<SerializationWrapper>(string)

            return Configuration(wrapper.enabledFeatures, wrapper.groups)
        }
    }
}
