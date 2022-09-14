package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.ConfigurationContainer
import co.touchlab.swiftgen.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassDescriptor

context(ConfigurationContainer)
fun <T> ClassDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    configuration[ClassDescriptorConfigurationTarget(this), key]