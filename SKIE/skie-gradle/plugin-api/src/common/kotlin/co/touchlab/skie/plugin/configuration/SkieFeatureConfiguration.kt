package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.configuration.util.GradleSkieConfigurationData
import co.touchlab.skie.plugin.configuration.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val coroutinesInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * For performance reasons SKIE does not generate default arguments for functions in external libraries even if enabled via the group configuration.
     * This behavior can be overridden by setting this property to true.
     *
     * Warning: Depending on the project this action can have a significant impact the compilation time because it turns off the Kotlin compiler caching.
     *
     * Note that even with this property turned on, it is still required to enable default arguments for individual functions from those libraries.
     * This can be done only via the group configuration from Gradle.
     * To opt in for some functions, use `group("$declarationFqNamePrefix") { DefaultArgumentInterop.Enabled(true) }`.
     * To opt in for all functions globally, use `group { DefaultArgumentInterop.Enabled(true) }`.
     */
    val defaultArgumentsInExternalLibraries: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

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

        internal fun build(): GradleSkieConfigurationData.Group = GradleSkieConfigurationData.Group(
            target = targetFqNamePrefix,
            overridesAnnotations = overridesAnnotations,
            items = items.toMap(),
        )
    }

    internal fun buildGroups(): List<GradleSkieConfigurationData.Group> =
        groupConfigurations.map { it.build() }

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        setOfNotNull(
            SkieConfigurationFlag.Feature_CoroutinesInterop takeIf coroutinesInterop,
            SkieConfigurationFlag.Feature_DefaultArgumentsInExternalLibraries takeIf defaultArgumentsInExternalLibraries,
        )
}
