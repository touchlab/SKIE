package co.touchlab.skie.plugin.configuration.util

import co.touchlab.skie.configuration.UntypedSkieConfigurationData
import co.touchlab.skie.configuration.SkieConfigurationFlag

data class GradleSkieConfigurationData(
    override val enabledConfigurationFlags: Set<SkieConfigurationFlag>,
    override val groups: List<Group>,
) : UntypedSkieConfigurationData<SkieConfigurationFlag> {

    data class Group(
        override val target: String,
        override val overridesAnnotations: Boolean,
        override val items: Map<String, String?>,
    ) : UntypedSkieConfigurationData.Group
}
