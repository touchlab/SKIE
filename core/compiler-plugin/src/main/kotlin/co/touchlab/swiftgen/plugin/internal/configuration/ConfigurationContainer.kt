package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKey
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration as getConfigurationWithConfiguration

interface ConfigurationContainer {

    val configuration: Configuration

    fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
        with(configuration) { this@getConfiguration.getConfigurationWithConfiguration(key) }

    val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.getConfiguration(ConfigurationKeys.ExperimentalFeatures.Enabled)
}