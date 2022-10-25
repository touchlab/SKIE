package co.touchlab.skie.configuration.builder

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.ConfigurationKey

class ConfigurationGroupBuilder(
    private val targetFqNamePrefix: String,
    private val overridesAnnotations: Boolean,
) {

    private val items = mutableMapOf<String, String?>()

    operator fun <T> ConfigurationKey<T>.invoke(value: T) {
        items[this.name] = this.serialize(value)
    }

    internal fun build(): Configuration.Group = Configuration.Group(
        targetFqNamePrefix = targetFqNamePrefix,
        overridesAnnotations = overridesAnnotations,
        items = items.toMap(),
    )
}
