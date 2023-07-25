package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import co.touchlab.skie.configuration.gradle.AnalyticsTier
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class SkieExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Disables SKIE plugin. Useful for checking if an error is a bug in SKIE, or in the Kotlin compiler.
     */
    val isEnabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    private val configurationBuilder = ConfigurationBuilder()

    fun configuration(builder: ConfigurationBuilder.() -> Unit) {
        configurationBuilder.apply(builder)
    }

    val features: SkieFeatureConfiguration = objects.newInstance(SkieFeatureConfiguration::class.java)

    fun features(action: Action<SkieFeatureConfiguration>) {
        action.execute(features)
    }

    val analytics: Property<AnalyticsTier> = objects.property(AnalyticsTier::class.java).convention(AnalyticsTier.All)

    val debug: SkieDebugConfiguration = objects.newInstance(SkieDebugConfiguration::class.java)

    fun debug(action: Action<in SkieDebugConfiguration>) {
        action.execute(debug)
    }

    // Putting these extensions here so that they don't pollute the extension's namespace. They can be used by wrapping in `with(SkieExtension) { ... }`
    companion object {
        fun Project.createExtension(): SkieExtension =
            project.extensions.create("skie", SkieExtension::class.java)

        fun SkieExtension.buildConfiguration(): SkieConfiguration =
            SkieConfiguration(configurationBuilder) +
                SkieConfiguration(
                    enabledFeatures = features.buildFeatureSet() + debug.buildFeatureSet() + analytics.get().buildFeatureSet(),
                )
    }
}
