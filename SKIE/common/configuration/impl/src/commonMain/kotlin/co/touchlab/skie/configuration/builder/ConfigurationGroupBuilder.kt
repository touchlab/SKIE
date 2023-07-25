package co.touchlab.skie.configuration.builder

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.ConfigurationKey

class ConfigurationGroupBuilder(
    private val targetFqNamePrefix: String,
    private val overridesAnnotations: Boolean,
) {

    private val items = mutableMapOf<String, String?>()

    operator fun <T> ConfigurationKey<T>.invoke(value: T) {
        items[this.name] = this.serialize(value)
    }

    internal fun build(): SkieConfiguration.Group = SkieConfiguration.Group(
        targetFqNamePrefix = targetFqNamePrefix,
        overridesAnnotations = overridesAnnotations,
        items = items.toMap(),
    )
}
