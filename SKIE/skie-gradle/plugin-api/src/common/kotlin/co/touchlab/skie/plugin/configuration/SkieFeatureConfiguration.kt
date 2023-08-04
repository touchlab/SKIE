package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.configuration.util.GradleSkieConfiguration
import co.touchlab.skie.plugin.configuration.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val coroutinesInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val fqNames: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    private val groupConfigurations = mutableListOf<GroupConfiguration>()

    fun group(targetFqNamePrefix: String = "", overridesAnnotations: Boolean = false, action: GroupConfiguration.() -> Unit) {
        val groupConfiguration = GroupConfiguration(targetFqNamePrefix, overridesAnnotations)

        groupConfigurations.add(groupConfiguration)

        groupConfiguration.action()
    }

    class GroupConfiguration(
        private val targetFqNamePrefix: String,
        private val overridesAnnotations: Boolean,
    ) {

        private val items = mutableMapOf<String, String?>()

        operator fun <T> ConfigurationKey<T>.invoke(value: T) {
            items[this.name] = this.serialize(value)
        }

        internal fun build(): GradleSkieConfiguration.Group = GradleSkieConfiguration.Group(
            target = targetFqNamePrefix,
            overridesAnnotations = overridesAnnotations,
            items = items.toMap(),
        )
    }

    internal fun buildGroups(): List<GradleSkieConfiguration.Group> =
        groupConfigurations.map { it.build() }

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        setOfNotNull(
            SkieConfigurationFlag.Feature_CoroutinesInterop takeIf coroutinesInterop,
            SkieConfigurationFlag.Feature_FqNames takeIf fqNames,
        )
}
