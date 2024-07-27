package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.UntypedSkieConfigurationData

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
