package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.configuration.util.GradleSkieConfiguration
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

open class SkieExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Disables SKIE plugin. Useful for checking if an error is a bug in SKIE, or in the Kotlin compiler.
     */
    val isEnabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    val additionalFeatureFlags: SetProperty<SkieFeature> = objects.setProperty(SkieFeature::class.java).convention(emptySet())

    val suppressedFeatureFlags: SetProperty<SkieFeature> = objects.setProperty(SkieFeature::class.java).convention(emptySet())

    private val declarationConfigurationBuilder = SkieDeclarationConfiguration()

    @Deprecated("replace with declaration {}")
    fun configuration(builder: SkieDeclarationConfiguration.() -> Unit) {
        declarationConfigurationBuilder.apply(builder)
    }

    // WIP Consider different name
    fun declaration(builder: SkieDeclarationConfiguration.() -> Unit) {
        declarationConfigurationBuilder.apply(builder)
    }

    val analytics: SkieAnalyticsConfiguration = objects.newInstance(SkieAnalyticsConfiguration::class.java)

    fun analytics(action: Action<in SkieAnalyticsConfiguration>) {
        action.execute(analytics)
    }

    val debug: SkieDebugConfiguration = objects.newInstance(SkieDebugConfiguration::class.java)

    fun debug(action: Action<in SkieDebugConfiguration>) {
        action.execute(debug)
    }

    val features: SkieFeatureConfiguration = objects.newInstance(SkieFeatureConfiguration::class.java)

    fun features(action: Action<SkieFeatureConfiguration>) {
        action.execute(features)
    }

    // Putting these extensions here so that they don't pollute the extension's namespace. They can be used by wrapping in `with(SkieExtension) { ... }`
    companion object {

        fun Project.createExtension(): SkieExtension =
            project.extensions.create("skie", SkieExtension::class.java)

        fun SkieExtension.buildConfiguration(): GradleSkieConfiguration =
            GradleSkieConfiguration(
                enabledFeatures = (mergeFeatureSetsFromConfigurations() + additionalFeatureFlags.get()) - suppressedFeatureFlags.get(),
                groups = declarationConfigurationBuilder.buildGroups(),
            )

        private fun SkieExtension.mergeFeatureSetsFromConfigurations() =
            analytics.buildFeatureSet() + debug.buildFeatureSet() + features.buildFeatureSet()
    }
}
