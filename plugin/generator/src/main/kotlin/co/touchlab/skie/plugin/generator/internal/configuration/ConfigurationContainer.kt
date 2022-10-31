package co.touchlab.skie.plugin.generator.internal.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.gradle.ExperimentalFeatures
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import co.touchlab.skie.plugin.generator.internal.configuration.getConfiguration as getConfigurationWithConfiguration

interface ConfigurationContainer {

    val configuration: Configuration

    fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
        with(configuration) { this@getConfiguration.getConfigurationWithConfiguration(key) }

    val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.getConfiguration(ExperimentalFeatures.Enabled)
}
