package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import co.touchlab.skie.configuration.gradle.AnalyticsTier
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
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

    val analytics: Property<AnalyticsTier> = objects.property(AnalyticsTier::class.java).convention(AnalyticsTier.Full)

    val debug: SkieDebugConfiguration = objects.newInstance(SkieDebugConfiguration::class.java)

    fun debug(action: Action<in SkieDebugConfiguration>) {
        action.execute(debug)
    }

    internal fun buildConfiguration(): Configuration =
        Configuration(configurationBuilder) +
            Configuration(
                enabledFeatures = features.buildFeatureSet() + debug.buildFeatureSet(),
                analyticsConfiguration = analytics.get().buildAnalyticsConfiguration(),
            )

    companion object {

        internal fun createExtension(project: Project): SkieExtension =
            project.extensions.create("skie", SkieExtension::class.java)
    }
}

internal val Project.skieExtension: SkieExtension
    get() = project.extensions.getByType(SkieExtension::class.java)
