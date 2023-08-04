package co.touchlab.skie.plugin.configuration.util

import co.touchlab.skie.configuration.TypedSkieConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag

data class GradleSkieConfiguration(
    override val enabledConfigurationFlags: Set<SkieConfigurationFlag>,
    override val groups: List<Group>,
) : TypedSkieConfiguration<SkieConfigurationFlag> {

    data class Group(
        override val target: String,
        override val overridesAnnotations: Boolean,
        override val items: Map<String, String?>,
    ) : TypedSkieConfiguration.Group
}
