package co.touchlab.skie.plugin.configuration.util

import co.touchlab.skie.configuration.TypedSkieConfiguration
import co.touchlab.skie.configuration.SkieFeature

data class GradleSkieConfiguration(
    override val enabledFeatures: Set<SkieFeature>,
    override val groups: List<Group>,
) : TypedSkieConfiguration<SkieFeature> {

    data class Group(
        override val target: String,
        override val overridesAnnotations: Boolean,
        override val items: Map<String, String?>,
    ) : TypedSkieConfiguration.Group
}
