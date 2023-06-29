package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import co.touchlab.skie.configuration.features.SkieFeatureSet
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Configuration(
    val enabledFeatures: SkieFeatureSet = SkieFeatureSet(),
    val disabledFeatures: SkieFeatureSet = SkieFeatureSet(),
    val groups: List<Group> = emptyList(),
    val analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration(),
) {

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
        groups.lastOrNull { target.fqName.startsWith(it.targetFqNamePrefix) && key.name in it.items }

    private fun <T> Group.findValue(key: ConfigurationKey<T>): T? =
        this.items[key.name]?.let { key.deserialize(it) }

    fun serialize(): String {
        val json = Json { prettyPrint = true }

        return json.encodeToString(this)
    }

    operator fun plus(other: Configuration): Configuration =
        Configuration(
            (enabledFeatures - other.disabledFeatures) + other.enabledFeatures,
            other.disabledFeatures,
            groups + other.groups,
            analyticsConfiguration + other.analyticsConfiguration,
        )

    @Serializable
    data class Group(
        @SerialName("target")
        val targetFqNamePrefix: String,
        val overridesAnnotations: Boolean,
        val items: Map<String, String?>,
    )

    companion object {

        operator fun invoke(builder: ConfigurationBuilder.() -> Unit): Configuration =
            ConfigurationBuilder().also(builder).build()

        operator fun invoke(builder: ConfigurationBuilder): Configuration =
            builder.build()

        fun deserialize(string: String): Configuration =
            Json.decodeFromString(string)
    }
}