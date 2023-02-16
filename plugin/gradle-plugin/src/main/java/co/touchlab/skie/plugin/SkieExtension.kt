package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.builder.ConfigurationBuilder
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class SkieExtension @Inject constructor(objects: ObjectFactory) {

    val isWildcardExportPrevented: Property<Boolean> = objects.property<Boolean>().convention(true)

    /**
     * Disables SKIE plugin. Useful for checking if an error is a bug in SKIE, or in the Kotlin compiler.
     */
    val isEnabled: Property<Boolean> = objects.property<Boolean>().convention(true)

    private val configurationBuilder = ConfigurationBuilder()

    fun configuration(builder: ConfigurationBuilder.() -> Unit) {
        configurationBuilder.apply(builder)
    }

    internal fun buildConfiguration(): Configuration =
        Configuration(configurationBuilder) + Configuration(features.buildFeatureSet(), emptyList())

    val features: SkieFeatureConfiguration = objects.newInstance(SkieFeatureConfiguration::class.java)

    fun features(action: Action<SkieFeatureConfiguration>) {
        action.execute(features)
    }

    val DependencyHandlerScope.skiePlugin: String
        get() = SKIE_PLUGIN_CONFIGURATION_NAME
}
