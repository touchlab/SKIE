package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import co.touchlab.swiftgen.plugin.internal.configuration.getConfiguration as getConfigurationWithConfiguration

interface ConfigurationContainer {

    val configuration: Configuration

    fun <T> ClassDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
        with(configuration) { this@getConfiguration.getConfigurationWithConfiguration(key) }
}