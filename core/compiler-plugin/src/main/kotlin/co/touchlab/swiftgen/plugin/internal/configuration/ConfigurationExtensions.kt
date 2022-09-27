package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassDescriptor

context(Configuration)
fun <T> ClassDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    this@Configuration[ClassDescriptorConfigurationTarget(this@getConfiguration), key]